package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.*
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.showClickableBalloonError
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

@Service(Service.Level.PROJECT)
class AsyncScanService(private val project: Project) {

    suspend fun scan(targets: Collection<VirtualFile>, configuration: ToolExecutorConfiguration): List<MypyMessage> {
        val nonJsonStdout = StringBuilder()
        val parameters = with(project) { buildMypyParamList(configuration, targets) }
        val stdErr = StringBuilder()
        val executor = MypyExecutor(project)
        return executor.execute(configuration, parameters).filter { it.text.isNotBlank() }
            .transform { line ->
                if (line.isError) {
                    stdErr.append(line.text)
                    return@transform
                }
                MypyOutputParser.parse(line.text).onSuccess { emit(it) }.onFailure {
                    when (it) {
                        is MypyParseException -> {
                            // mypy sometimes ignores -O json for certain errors; collect the raw lines as-is
                            nonJsonStdout.appendLine(it.sourceJson)
                        }

                        else -> {
                            thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                        }
                    }
                }
            }.onCompletion {
                val output = buildString {
                    if (stdErr.isNotEmpty()) append(stdErr)
                    if (nonJsonStdout.isNotEmpty()) {
                        if (stdErr.isNotEmpty()) appendLine()
                        append(nonJsonStdout)
                    }
                }
                if (output.isNotEmpty()) {
                    showClickableBalloonError(project, MypyToolWindowPanel.ID, MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
                        DialogManager.showToolExecutionErrorDialog(configuration, output, executor.exitCode)
                    }
                }
            }.catch(handleScanException(project, configuration, stdErr, MypyIncompleteConfigurationNotifier.getInstance(project))).toList(ArrayList())
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): AsyncScanService = project.service()
    }
}