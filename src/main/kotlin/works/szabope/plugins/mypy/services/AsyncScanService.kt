package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.*
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException

@Service(Service.Level.PROJECT)
class AsyncScanService(private val project: Project) {

    suspend fun scan(targets: Collection<VirtualFile>, configuration: ImmutableSettingsData): List<MypyMessage> {
        // Why? See MypyParseException
        // So let's collect parse failures and report them.
        // If you have a better idea, please let me know.
        val unparsableLinesOfStdout = StringBuilder()
        val parameters = with(project) { buildMypyParamList(configuration, targets) }
        val stdErr = StringBuilder()
        return MypyExecutor(project).execute(configuration, parameters).filter { it.text.isNotBlank() }
            .transform { line ->
                if (line.isError) {
                    stdErr.append(line.text)
                    return@transform
                }
                MypyOutputParser.parse(line.text).onSuccess { emit(it) }.onFailure {
                    when (it) {
                        is MypyParseException -> {
                            unparsableLinesOfStdout.appendLine("${it.sourceJson} failed with ${it.message}")
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
                            configuration,
                            targets.joinToString("\n"),
                            unparsableLinesOfStdout.toString(),
                            "" //TODO: this looks ugly -> make it less ugly
                        )
                    }
                }
            }.catch(handleScanException(project, configuration, stdErr)).toList(ArrayList())
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): AsyncScanService = project.service()
    }
}