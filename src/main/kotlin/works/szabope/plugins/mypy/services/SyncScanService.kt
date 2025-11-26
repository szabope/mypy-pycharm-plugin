package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.asCompletableFuture
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@Service(Service.Level.PROJECT)
class SyncScanService(private val project: Project, private val cs: CoroutineScope) {

    fun scan(targets: Collection<VirtualFile>, configuration: ImmutableSettingsData): List<MypyMessage> {
        val shadowedTargetMap = targets.associateWith {
            measureTimedValue {
                copyTempFrom(it)
            }.also { m -> thisLogger().debug($$"SyncScanService#scan$createShadowFile took $${m.duration}") }.value
        }
        val parameters = measureTimedValue {
            with(project) { buildMypyParamList(configuration, shadowedTargetMap) }
        }.also { m -> thisLogger().debug($$"SyncScanService#scan$buildParamList took $${m.duration}") }.value
        val stdErr = StringBuilder()
        val flow = MypyExecutor(project).execute(configuration, parameters).filter { it.text.isNotBlank() }
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
                measureTime {
                    // cleanup
                    shadowedTargetMap.values.onEach { shadowFile -> shadowFile.deleteIfExists() }
                }.let { thisLogger().debug($$"SyncScanService#scan$cleanUp took $$it") }
            }.catch(handleScanException(project, configuration, stdErr))
        return cs.async {
            flow.toList()
        }.asCompletableFuture().get()
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
