package works.szabope.plugins.mypy.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiFile
import com.intellij.util.io.delete
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.MypySettings.SettingsValidationException
import works.szabope.plugins.mypy.services.cli.MypyOutput
import works.szabope.plugins.mypy.services.cli.PyVirtualEnvCli
import works.szabope.plugins.mypy.toRunConfiguration
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.fileSize
import kotlin.io.path.writeText

internal class MypyAnnotator : ExternalAnnotator<MypyAnnotator.MypyAnnotatorInfo, List<MypyOutput>>() {

    private val logger = logger<PyVirtualEnvCli>()

    class MypyAnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): MypyAnnotatorInfo {
        return MypyAnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: MypyAnnotatorInfo): List<MypyOutput> {
        val settings = MypySettings.getInstance(info.project)
        try {
            settings.ensureValid()
        } catch (e: SettingsValidationException) {
            ToolWindowManager.getInstance(info.project).notifyByBalloon(
                MypyToolWindowPanel.ID,
                MessageType.WARNING,
                MyBundle.message("mypy.toolwindow.balloon.error", e.message!!, e.blame)
            )
        }
        if (!settings.isInitialized()) {
            return emptyList()
        }
        val service = MypyService.getInstance(info.project)
        val runConfiguration = MypySettings.getInstance(info.project).toRunConfiguration()
        val scan = fun(absolutePath: String): List<MypyOutput> = service.scan(absolutePath, runConfiguration)
        val content = getCachedContent(info.file)
        if (content == null) {
            logger.debug("File was not cached, running scan for ${info.file.path}")
            return scan(info.file.path)
        }
        val tempFile = createTempFile("mypy-pycharm-plugin-editor-scan", ".py")
        try {
            tempFile.writeText(content)
            logger.debug(
                "File ${info.file.path} found in cache: content of ${tempFile.fileSize()} " + "bytes were written to ${tempFile.toCanonicalPath()}"
            )
            return scan(tempFile.absolutePathString())
        } finally {
            tempFile.delete()
        }
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyOutput>, holder: AnnotationHolder) {
        logger.debug("Mypy returned ${annotationResult.size} issues for ${file.virtualFile.canonicalPath}")
        MypyService.getInstance(file.project).annotate(file, annotationResult, holder)
    }

    override fun getPairedBatchInspectionShortName(): String {
        return MyBundle.message("mypy.inspection.id")
    }

    @Suppress("UnstableApiUsage")
    private fun getCachedContent(file: VirtualFile): String? {
        return runBlockingCancellable {
            readAction {
                // we don't want to load the file into memory, but if it's already there we need that version
                FileDocumentManager.getInstance().getCachedDocument(file)?.text
            }
        }
    }
}