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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.ScanService
import works.szabope.plugins.common.services.tool.ToolOutputHandler
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.run.MypyExecutionEnvironmentFactory
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import javax.swing.event.HyperlinkEvent

@Service(Service.Level.PROJECT)
class AsyncScanService(private val project: Project, private val cs: CoroutineScope) : ScanService<MypyMessage> {

    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    override fun scan(
        targets: Collection<VirtualFile>,
        configuration: ImmutableSettingsData,
        resultHandler: ToolOutputHandler<MypyMessage>
    ) {
        val environment = MypyExecutionEnvironmentFactory(project).createEnvironment(configuration, targets)
        manualScanJob = cs.launch {
            execute(environment).onFailure {
                if (it is ProcessException) {
                    showClickableBalloonError(MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
                        DialogManager.showToolExecutionErrorDialog(
                            configuration, it.stdErr, it.exitCode
                        )
                    }
                } else {
                    thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                }
            }.onSuccess { raw ->
                // mypy is not willing to distinguish between throwing an error and reporting one
                // - can't rely on process status != 0; https://github.com/python/mypy/issues/6003
                // - mypy exceptions _sometimes_ printed to stdout, mixing them into normal output, in which case even `-O json` is ignored
                // - and sometimes after such and exception comes valuable json output
                // so let's collect parse failures and report them
                // If you have a better idea, please let me know.
                val unparsableLinesOfStdout = StringBuilder()
                MypyOutputParser.parse(raw).onEach { message ->
                    message.onFailure {
                        if (it is MypyParseException) {
                            unparsableLinesOfStdout.appendLine(it.sourceJson)
                        } else {
                            thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                        }
                    }
                }.filter { it.isSuccess }.map { it.getOrThrow() }.let { resultHandler.handle(it) }
                if (unparsableLinesOfStdout.isNotEmpty()) {
                    showClickableBalloonError(MypyBundle.message("mypy.toolwindow.balloon.parse_error")) {
                        DialogManager.showToolOutputParseErrorDialog(
                            configuration, targets.joinToString(" "), "", unparsableLinesOfStdout.toString()
                        )
                    }
                }
            }
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