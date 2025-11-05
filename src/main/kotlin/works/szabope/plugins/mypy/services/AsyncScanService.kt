package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import works.szabope.plugins.common.processErrorAndGet
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import works.szabope.plugins.mypy.services.tool.MypyPublishingToolOutputHandler

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
            val parameters = with(project) { buildMypyParamList(configuration, targets) }
            val output = MypyExecutor(project).execute(configuration, parameters).processErrorAndGet {
                showClickableBalloonError(
                    project, MypyBundle.message("mypy.toolwindow.balloon.failed_to_execute")
                ) {
                    DialogManager.showFailedToExecuteErrorDialog(
                        it.message ?: MypyBundle.message("mypy.please_report_this_issue")
                    )
                }
                return@launch
            }

            // exit code 1 should be fine https://github.com/python/mypy/issues/6003
            if (output.exitCode > 1) {
                showClickableBalloonError(project, MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
                    DialogManager.showToolExecutionErrorDialog(
                        configuration, output.stderr, output.exitCode
                    )
                }
            }
            output.stdoutLines.asFlow().transform { line ->
                MypyOutputParser.parse(line).onSuccess { emit(it) }.onFailure {
                    when (it) {
                        is MypyParseException -> {
                            unparsableLinesOfStdout.appendLine(it.sourceJson)
                        }

                        else -> {
                            thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                        }
                    }
                }
            }.onCompletion {
                if (unparsableLinesOfStdout.isNotEmpty()) {
                    showClickableBalloonError(project, MypyBundle.message("mypy.toolwindow.balloon.parse_error")) {
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

    companion object {
        @JvmStatic
        fun getInstance(project: Project): AsyncScanService = project.service()
    }
}