package works.szabope.plugins.mypy.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.future
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.parser.MypyMessage
import works.szabope.plugins.mypy.services.parser.MypyOutputParser
import works.szabope.plugins.mypy.services.parser.MypyParseException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

@Service(Service.Level.PROJECT)
class SyncScanService(private val project: Project, private val cs: CoroutineScope) {

    fun scan(
        targets: Collection<VirtualFile>, configuration: ImmutableSettingsData
    ): Map<VirtualFile, List<MypyMessage>> {
        val shadowedTargetMap = targets.associateWith {
            copyTempFrom(it)
        }
        val parameters = with(project) { buildMypyParamList(configuration, shadowedTargetMap) }
        val stdErr = StringBuilder()
        val flow: Flow<Pair<VirtualFile, MypyMessage>> =
            MypyExecutor(project).execute(configuration, parameters).filter { it.text.isNotBlank() }.transform { line ->
                if (line.isError) {
                    stdErr.append(line.text)
                    return@transform
                }
                MypyOutputParser.parse(line.text).onSuccess { message ->
                    findFile(Path(message.file))?.let { virtualFile ->
                        emit(virtualFile to message)
                    } ?: thisLogger().warn("Can't find VirtualFile at ${message.file}")
                }.onFailure {
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
            }.catch(handleScanException(project, configuration, stdErr))
        return cs.future {
            flow.fold(mutableMapOf<VirtualFile, MutableList<MypyMessage>>()) { acc, (k, v) ->
                acc.getOrPut(k) { mutableListOf() }.add(v)
                acc
            }.mapValues { (_, v) -> v.toList() }
        }.get()
    }

    private fun findFile(path: Path): VirtualFile? {
        return if (ApplicationManager.getApplication().isUnitTestMode) {
            @Suppress("UnstableApiUsage")
            TempFileSystem.getInstance().findFileByNioFile(path)
        } else {
            VfsUtil.findFile(path, false)
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
