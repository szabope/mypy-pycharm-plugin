package works.szabope.plugins.mypy.services

import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.transform
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
        val output = try {
            with(MypyExecutor(project)) {
                val parameters = buildMypyParameters(configuration, shadowedTargetMap)
                execute(configuration, parameters)
            }
        } catch (e: ProcessNotCreatedException) {
            showClickableBalloonError(project, MypyBundle.message("mypy.toolwindow.balloon.failed_to_execute")) {
                DialogManager.showFailedToExecuteErrorDialog(
                    e.message ?: MypyBundle.message("mypy.please_report_this_issue")
                )
            }
            return emptyFlow()
        } finally {
            shadowedTargetMap.values.onEach { it.deleteIfExists() }
        }
        // exit code 1 should be fine https://github.com/python/mypy/issues/6003
        if (output.exitCode > 1) {
            showClickableBalloonError(project, MypyBundle.message("mypy.toolwindow.balloon.external_error")) {
                DialogManager.showToolExecutionErrorDialog(
                    configuration, output.stderr, output.exitCode
                )
            }
        }
        return output.stdoutLines.asFlow().transform { line -> // transform
            MypyOutputParser.parse(line).onSuccess { emit(it) }.onFailure {
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
