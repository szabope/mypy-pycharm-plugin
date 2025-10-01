package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import works.szabope.plugins.mypy.services.tool.MypyPublishingToolOutputHandler
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import javax.swing.event.HyperlinkEvent

@Service(Service.Level.PROJECT)
class AsyncScanService(private val project: Project, private val cs: CoroutineScope) {

    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    fun scan(targets: Collection<VirtualFile>, configuration: ImmutableSettingsData) {
        manualScanJob = cs.launch {
            // Why? See MypyParseException
            // So let's collect parse failures and report them.
            // If you have a better idea, please let me know.
            val unparsableLinesOfStdout = StringBuilder()

            with(MypyExecutor(project)) {
                val parameters = buildMypyParameters(configuration, targets)
                execute(configuration, parameters)
            }.filter { it.isNotBlank() }.asFlow().transform { line ->
                MypyOutputParser.parse(line).onFailure {
                    when (it) {
                        is MypyParseException -> {
                            unparsableLinesOfStdout.appendLine(it.sourceJson)
                        }

                        else -> {
                            thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                        }
                    }
                }.onSuccess { emit(it) }
            }.buffer(capacity = Channel.UNLIMITED).catch {
                thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
            }.onCompletion {
                if (unparsableLinesOfStdout.isNotEmpty()) {
                    showClickableBalloonError(MypyBundle.message("mypy.toolwindow.balloon.parse_error")) {
                        DialogManager.showToolOutputParseErrorDialog(
                            configuration, targets.joinToString("\n"), unparsableLinesOfStdout.toString(), ""
                        )
                    }
                }
            }.let { MypyPublishingToolOutputHandler(project).handle(it) }
        }
    }

    fun cancelScan() {
        cs.launch {
            manualScanJob?.cancelAndJoin()
        }
    }

    private fun showClickableBalloonError(balloonMessage: String, onClick: () -> Unit) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
            MypyToolWindowPanel.ID, MessageType.ERROR, balloonMessage, null
        ) {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                onClick()
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): AsyncScanService = project.service()
    }
}