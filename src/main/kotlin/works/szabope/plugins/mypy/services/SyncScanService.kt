package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.*
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.run.MypyExecutionEnvironmentFactory
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

@Service(Service.Level.PROJECT)
class SyncScanService(private val project: Project) {

    fun scan(targets: Collection<VirtualFile>, configuration: ImmutableSettingsData): Flow<MypyMessage> {
        val shadowedTargetMap = targets.associateWith { castShadowFor(it) }
        val environment = MypyExecutionEnvironmentFactory(project).createEnvironment(configuration, shadowedTargetMap)
        return execute(environment, true).filter { it.isNotBlank() }.transform { line -> // transform
            MypyOutputParser.parse(line).onFailure {
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
            }.onSuccess { emit(it) }
        }.catch {
            thisLogger().error(MypyBundle.message("mypy.please_report_this_issue"), it)
        }.onCompletion { shadowedTargetMap.values.onEach { it.deleteIfExists() } }
    }

    private fun castShadowFor(file: VirtualFile): Path {
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
