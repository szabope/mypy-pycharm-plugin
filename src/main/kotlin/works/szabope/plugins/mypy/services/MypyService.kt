package works.szabope.plugins.mypy.services

import com.intellij.grazie.utils.trimToNull
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.util.io.delete
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
import kotlin.io.path.pathString
import kotlin.io.path.writeText

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
    fun scan(file: VirtualFile, runConfiguration: RunConfiguration): List<MypyOutput> {
        val targetPath = file.path
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = requireNotNull(fileDocumentManager.getCachedDocument(file)) {
            "Please, report this issue at https://github.com/szabope/mypy-pycharm-plugin/issues"
        }
        val tempFile = kotlin.io.path.createTempFile(prefix = "pycharm_mypy_", suffix = "_" + file.name)
        try {
            tempFile.toFile().deleteOnExit()
            tempFile.writeText(document.charsSequence)
            val shadowArg = listOf("--shadow-file", targetPath, tempFile.pathString)
            val command = buildCommand(runConfiguration, listOf(targetPath), shadowArg)
            val handler = CollectingMypyOutputHandler()
            val result = runBlockingCancellable { execute(command, runConfiguration.projectDirectory, handler) }
            result.getError()?.also {
                logger.warn(MyBundle.message("mypy.executable.error", command, result.resultCode, it))
            }
            return handler.getResults()
        } finally {
            tempFile.delete()
        }
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
                        IDialogManager.showMypyExecutionErrorDialog(
                            command.joinToString(" "),
                            result.getError() ?: "",
                            result.resultCode
                        )
                    }
                }
            }
        }
    }

    fun cancelScan() {
        manualScanJob?.cancel()
    }

    private fun buildCommand(
        runConfiguration: RunConfiguration,
        targets: List<String>,
        extraArgs: List<String> = emptyList()
    ): List<String> =
        with(runConfiguration) {
            val command = mutableListOf(mypyExecutable)
            command.addAll(MypyArgs.MYPY_MANDATORY_COMMAND_ARGS)
            configFilePath.nullize(true)?.let { path -> command.addAll(listOf("--config-file", path)) }
            arguments.nullize(true)?.let { args -> command.addAll(args.split(" ")) }
            if (excludeNonProjectFiles) {
                targets.flatMap { collectExclusionsFor(it) }.union(customExclusions)
                    .forEach { exclusion ->
                        command.add("--exclude")
                        command.add(exclusion)
                    }
            }
            extraArgs.forEach { arg -> command.add(arg) }
            command.addAll(targets)
            command
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
            }
        }
        return exclusions
    }

    private suspend fun execute(command: List<String>, workDir: String, stdoutHandler: IMypyOutputHandler): MypyStatus =
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
