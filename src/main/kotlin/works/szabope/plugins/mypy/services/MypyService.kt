package works.szabope.plugins.mypy.services

import com.intellij.grazie.utils.trimToNull
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.util.text.nullize
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.dialog.IDialogManager
import works.szabope.plugins.mypy.services.cli.PythonEnvironmentAwareCli
import works.szabope.plugins.mypy.services.parser.CollectingMypyOutputHandler
import works.szabope.plugins.mypy.services.parser.IMypyOutputHandler
import works.szabope.plugins.mypy.services.parser.MypyOutput
import works.szabope.plugins.mypy.services.parser.PublishingMypyOutputHandler
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import javax.swing.event.HyperlinkEvent
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

    class MypyStatus(val resultCode: Int, private val stderr: String, private val handlerError: String) {
        fun getError() = (stderr + "\n" + handlerError).trimToNull()
    }

    @Suppress("UnstableApiUsage")
    fun scan(filePath: String, runConfiguration: RunConfiguration): List<MypyOutput> {
        val command = buildCommand(runConfiguration, listOf(filePath))
        val handler = CollectingMypyOutputHandler()
        val result = runBlockingCancellable { execute(command, runConfiguration.projectDirectory, handler) }
        result.getError()?.also {
            logger.warn(MyBundle.message("mypy.executable.error", command, result.resultCode, it))
        }
        return handler.getResults()
    }

    fun scanAsync(scanPaths: List<String>, runConfiguration: RunConfiguration) {
        val command = buildCommand(runConfiguration, scanPaths)
        val handler = PublishingMypyOutputHandler(project)
        manualScanJob = cs.launch {
            val result = execute(command, runConfiguration.projectDirectory, handler)
            logger.debug("${handler.resultCount} issues found")
            if (result.getError() != null) {
                ToolWindowManager.getInstance(project).notifyByBalloon(
                    MypyToolWindowPanel.ID, MessageType.ERROR, MyBundle.message("mypy.toolwindow.balloon.error"), null
                ) {
                    if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                        IDialogManager.showMypyExecutionErrorDialog(command, result.getError() ?: "", result.resultCode)
                    }
                }
            }
        }
    }

    fun cancelScan() {
        manualScanJob?.cancel()
    }

    private fun buildCommand(runConfiguration: RunConfiguration, targets: List<String>) = with(runConfiguration) {
        val commandBuilder = StringBuilder(mypyExecutable).append(" ").append(MypyArgs.MYPY_MANDATORY_COMMAND_ARGS)
        configFilePath.nullize(true)?.apply { commandBuilder.append(" --config-file $this") }
        arguments.nullize(true)?.apply { commandBuilder.append(" $this") }
        if (excludeNonProjectFiles) {
            targets.flatMap { collectExclusionsFor(it) }.union(customExclusions)
                .forEach { commandBuilder.append(" --exclude $it") }
        }
        commandBuilder.append(" ").append(targets.joinToString(" ")).toString()
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

    private suspend fun execute(command: String, workDir: String, stdoutHandler: IMypyOutputHandler): MypyStatus =
        PythonEnvironmentAwareCli(project).execute(command, workDir, stdoutHandler::handle).let {
            MypyStatus(it.resultCode, it.stderr, stdoutHandler.getError())
        }

    companion object {
        @JvmStatic
        val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

        @JvmStatic
        fun getInstance(project: Project): MypyService = project.service()
    }
}
