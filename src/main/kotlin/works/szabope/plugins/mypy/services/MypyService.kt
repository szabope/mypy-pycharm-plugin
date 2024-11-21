package works.szabope.plugins.mypy.services

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import com.intellij.util.text.nullize
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.annotator.MypyIgnoreIntention
import works.szabope.plugins.mypy.services.cli.CollectingMypyOutputHandler
import works.szabope.plugins.mypy.services.cli.MypyOutput
import works.szabope.plugins.mypy.services.cli.PublishingMypyOutputHandler
import works.szabope.plugins.mypy.services.cli.PyVirtualEnvCli
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

@Service(Service.Level.PROJECT)
class MypyService(private val project: Project, private val cs: CoroutineScope) {

    private var manualScanJob: Job? = null

    val scanInProgress: Boolean
        get() = manualScanJob?.isActive == true

    class RunConfiguration(
        val mypyExecutable: String, val configFilePath: String? = null, val arguments: String? = null
    )

    class MypyServiceException(message: String) : RuntimeException(message)

    @Suppress("UnstableApiUsage")
    fun scan(filePath: String, runConfiguration: RunConfiguration): List<MypyOutput> {
        val command = buildCommand(runConfiguration, listOf(filePath))
        val handler = CollectingMypyOutputHandler()
        runBlockingCancellable {
            PyVirtualEnvCli(project).execute(command) { handler.handle(it) }
        }
        if (handler.getError().isNotEmpty()) {
            logger<MypyService>().error("Failing command: $command", MypyServiceException(handler.getError()))
        }
        return handler.getResults()
    }

    fun scanAsync(scanPaths: List<String>, runConfiguration: RunConfiguration) {
        val command = buildCommand(runConfiguration, scanPaths)
        val handler = PublishingMypyOutputHandler(project)
        manualScanJob = cs.launch {
            PyVirtualEnvCli(project).execute(command) { handler.handle(it) }
            if (handler.getError().isNotEmpty()) {
                logger<MypyService>().error("Failing command: $command", MypyServiceException(handler.getError()))
                ToolWindowManager.getInstance(project).notifyByBalloon(
                    MypyToolWindowPanel.ID,
                    MessageType.ERROR,
                    "Mypy executable has thrown an error. For details see notifications."
                )
            }
        }
    }

    fun cancelScan() {
        manualScanJob?.cancel()
    }

    fun annotate(file: PsiFile, annotationResult: List<MypyOutput>, holder: AnnotationHolder) {
        val profile = InspectionProjectProfileManager.getInstance(file.project).currentProfile
        val severity = HighlightDisplayKey.findById(MyBundle.message("mypy.inspection.id"))?.let {
            profile.getErrorLevel(it, file).severity
        } ?: HighlightSeverity.ERROR

        annotationResult.forEach { issue ->
            val psiElement = file.findElementFor(issue) ?: return@forEach
            holder.newAnnotation(severity, issue.message).range(psiElement.textRange)
                .withFix(MypyIgnoreIntention(issue.line)).create()
        }
    }

    private fun buildCommand(runConfiguration: RunConfiguration, targets: List<String>): String {
        val commandBuilder =
            StringBuilder(runConfiguration.mypyExecutable).append(" ").append(MypyArgs.MYPY_MANDATORY_COMMAND_ARGS)
        runConfiguration.configFilePath.nullize(true)?.apply { commandBuilder.append(" --config-file $this") }
        runConfiguration.arguments.nullize(true)?.apply { commandBuilder.append(" ").append(this) }
        commandBuilder.append(" ").append(targets.joinToString(" "))
        return commandBuilder.toString()
    }

    private fun PsiFile.findElementFor(issue: MypyOutput): PsiElement? {
        val tabSize = CodeStyle.getFacade(this).tabSize
        val offset = DocumentUtil.calculateOffset(fileDocument, issue.line, issue.column, tabSize)
        return findElementAt(offset)
    }

    companion object {
        @JvmStatic
        val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

        @JvmStatic
        fun getInstance(project: Project): MypyService = project.service()
    }
}
