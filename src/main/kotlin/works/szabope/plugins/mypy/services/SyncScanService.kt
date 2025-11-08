package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transform
import works.szabope.plugins.common.run.ToolExecutionTerminatedException
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

@Service(Service.Level.PROJECT)
class SyncScanService(private val project: Project) {

    fun scan(targets: Collection<VirtualFile>, configuration: ImmutableSettingsData): Flow<MypyMessage> {
        val shadowedTargetMap = targets.associateWith { copyTempFrom(it) }
        val parameters = with(project) { buildMypyParamList(configuration, shadowedTargetMap) }
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
                            thisLogger().warn(
                                MypyBundle.message("mypy.executable.parsing-result-failed", configuration), it
                            )
                        }

                        else -> {
                            thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
                        }
                    }
                }
            }.onCompletion {
            // cleanup
            shadowedTargetMap.values.onEach { shadowFile -> shadowFile.deleteIfExists() }
            if (it is CancellationException) {
                throw it
            }
            if (it is ToolExecutionTerminatedException) {
                showClickableBalloonError(project, MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
                    DialogManager.showToolExecutionErrorDialog(
                        configuration, stdErr.toString(), it.exitCode
                    )
                }
            } else if (it != null) {
                // Unexpected exception
                showClickableBalloonError(project, MypyBundle.message("mypy.toolwindow.balloon.failed_to_execute")) {
                    DialogManager.showFailedToExecuteErrorDialog(
                        it.message ?: MypyBundle.message("mypy.please_report_this_issue")
                    )
                }
            }
        }
    }

    private fun copyTempFrom(file: VirtualFile): Path {
        val document = requireNotNull(FileDocumentManager.getInstance().getCachedDocument(file)) {
            MypyBundle.message("mypy.please_report_this_issue")
        }
        val tempFile = kotlin.io.path.createTempFile(prefix = "pycharm_mypy_", suffix = "_" + file.name)
        tempFile.toFile().deleteOnExit()
        tempFile.writeText(document.charsSequence)
        return tempFile
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): SyncScanService = project.service()
    }
}
