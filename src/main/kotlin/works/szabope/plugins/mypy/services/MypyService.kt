package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.util.text.nullize
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import works.szabope.plugins.mypy.services.cli.*
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class MypyService(private val project: Project, private val cs: CoroutineScope) {

    private val logger = logger<MypyService>()

    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    data class RunConfiguration(
        val mypyExecutable: String,
        val configFilePath: String? = null,
        val arguments: String? = null,
        val excludeNonProjectFiles: Boolean = true,
        val customExclusions: List<String> = listOf(),
        val projectDirectory: String
    )

    @Suppress("UnstableApiUsage")
    fun scan(
        filePath: String,
        runConfiguration: RunConfiguration,
        handleAnyFailures: (command: String, status: Int?, error: String) -> Unit
    ): List<MypyOutput> {
        val command = buildCommand(runConfiguration, listOf(filePath))
        val handler = CollectingMypyOutputHandler()
        val result = runBlockingCancellable {
            PyVirtualEnvCli(project).execute(command, runConfiguration.projectDirectory) { handler.handle(it) }
        }
        handleAnyFailures(command, result.resultCode, concatErrors(result, handler))
        return handler.getResults()
    }

    fun scanAsync(
        scanPaths: List<String>,
        runConfiguration: RunConfiguration,
        handleAnyFailures: (command: String, status: Int?, error: String) -> Unit
    ) {
        val command = buildCommand(runConfiguration, scanPaths)
        val handler = PublishingMypyOutputHandler(project)
        manualScanJob = cs.launch {
            val result =
                PyVirtualEnvCli(project).execute(command, runConfiguration.projectDirectory) { handler.handle(it) }
            logger.debug("${handler.resultCount} issues found")
            handleAnyFailures(command, result.resultCode, concatErrors(result, handler))
        }
    }

    fun cancelScan() {
        manualScanJob?.cancel()
    }

    private fun concatErrors(
        resultInStderr: PyVirtualEnvCli.Status, resultInStdout: AbstractMypyOutputHandler
    ) = arrayOf(resultInStderr.stderr, resultInStdout.getError()).joinToString("\n").trim()

    private fun buildCommand(runConfiguration: RunConfiguration, targets: List<String>): String {
        val (mypyExecutable, configFilePath, arguments, excludeNonProjectFiles, customExclusions) = runConfiguration
        val commandBuilder = StringBuilder(mypyExecutable).append(" ").append(MypyArgs.MYPY_MANDATORY_COMMAND_ARGS)
        configFilePath.nullize(true)?.apply { commandBuilder.append(" --config-file $this") }
        arguments.nullize(true)?.apply { commandBuilder.append(" $this") }
        if (excludeNonProjectFiles) {
            targets.flatMap { collectExclusionsFor(it) }.union(customExclusions)
                .forEach { commandBuilder.append(" --exclude $it") }
        }
        commandBuilder.append(" ").append(targets.joinToString(" "))
        return commandBuilder.toString()
    }

    private fun collectExclusionsFor(target: String): List<String> {
        val exclusions = mutableListOf<String>()
        val workspaceModel = WorkspaceModel.getInstance(project)
        val targetUrl = workspaceModel.getVirtualFileUrlManager().fromPath(target)
        workspaceModel.currentSnapshot.getVirtualFileUrlIndex().findEntitiesByUrl(targetUrl).forEach { entity ->
            if (entity is ContentRootEntity) {
                val contentRootPath = entity.url.virtualFile?.path?.let { Path(it) } ?: return@forEach
                entity.excludedUrls.mapNotNull { it.url.virtualFile?.path }.map { Path(it) }
                    .forEach { exclusions.add(contentRootPath.relativize(it).toCanonicalPath()) }
            } else {
                logger.info("nay")
            }
        }
        return exclusions
    }

    companion object {
        @JvmStatic
        val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

        @JvmStatic
        fun getInstance(project: Project): MypyService = project.service()
    }
}
