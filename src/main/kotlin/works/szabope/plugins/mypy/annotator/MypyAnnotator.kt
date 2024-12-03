package works.szabope.plugins.mypy.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.MypySettings.SettingsValidationException
import works.szabope.plugins.mypy.services.cli.MypyOutput
import works.szabope.plugins.mypy.services.cli.PyVirtualEnvCli
import works.szabope.plugins.mypy.toRunConfiguration

internal class MypyAnnotator : ExternalAnnotator<MypyAnnotator.MypyAnnotatorInfo, List<MypyOutput>>() {

    private val logger = logger<PyVirtualEnvCli>()

    class MypyAnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): MypyAnnotatorInfo {
        return MypyAnnotatorInfo(file.virtualFile, file.project)
    }

    @Suppress("UnstableApiUsage")
    override fun doAnnotate(info: MypyAnnotatorInfo): List<MypyOutput> {
        val settings = MypySettings.getInstance(info.project)
        try {
            settings.ensureValid()
        } catch (e: SettingsValidationException) {
            logger.warn(MyBundle.message("mypy.toolwindow.balloon.error", e.message!!, e.blame))
        }
        if (!settings.isInitialized()) {
            return emptyList()
        }

        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = fileDocumentManager.getCachedDocument(info.file)
        if (document != null) {
            runBlockingCancellable {
                writeAction {
                    fileDocumentManager.saveDocument(document)
                }
            }
        }
        val service = MypyService.getInstance(info.project)
        val runConfiguration = MypySettings.getInstance(info.project).toRunConfiguration()
        return service.scan(info.file.path, runConfiguration) { command, status, error ->
            logger.warn(MyBundle.message("mypy.error.stderr", command, status ?: 0, error))
        }
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyOutput>, holder: AnnotationHolder) {
        logger.debug("Mypy returned ${annotationResult.size} issues for ${file.virtualFile.canonicalPath}")
        MypyService.getInstance(file.project).annotate(file, annotationResult, holder)
    }

    override fun getPairedBatchInspectionShortName(): String {
        return MyBundle.message("mypy.inspection.id")
    }
}