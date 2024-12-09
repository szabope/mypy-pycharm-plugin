package works.szabope.plugins.mypy.annotator

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.MypySettings.SettingsValidationException
import works.szabope.plugins.mypy.services.parser.MypyOutput
import works.szabope.plugins.mypy.toRunConfiguration

internal class MypyAnnotator : ExternalAnnotator<MypyAnnotator.MypyAnnotatorInfo, List<MypyOutput>>() {

    private val logger = logger<MypyAnnotator>()

    class MypyAnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): MypyAnnotatorInfo {
        return MypyAnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: MypyAnnotatorInfo): List<MypyOutput> {
        val settings = MypySettings.getInstance(info.project)
        try {
            settings.ensureValid()
        } catch (e: SettingsValidationException) {
            logger.warn(MyBundle.message("${e.message!!}\n${e.blame}"))
        }
        if (!settings.isComplete()) {
            return emptyList()
        }

        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = fileDocumentManager.getCachedDocument(info.file)
        if (document != null) {
            val application = ApplicationManager.getApplication()
            application.invokeLater(
                {
                    runWriteAction {
                        runInEdt {
                            fileDocumentManager.saveDocument(document)
                        }
                    }
                }, application.defaultModalityState
            )
        }
        val service = MypyService.getInstance(info.project)
        val runConfiguration = MypySettings.getInstance(info.project).toRunConfiguration()
        return service.scan(info.file.path, runConfiguration)
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyOutput>, holder: AnnotationHolder) {
        logger.debug("Mypy returned ${annotationResult.size} issues for ${file.virtualFile.canonicalPath}")
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

    override fun getPairedBatchInspectionShortName(): String {
        return MyBundle.message("mypy.inspection.id")
    }

    private fun PsiFile.findElementFor(issue: MypyOutput): PsiElement? {
        val tabSize = CodeStyle.getFacade(this).tabSize
        val offset = DocumentUtil.calculateOffset(fileDocument, issue.line, issue.column, tabSize)
        return findElementAt(offset)
    }
}
