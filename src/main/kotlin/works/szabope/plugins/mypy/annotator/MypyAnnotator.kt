package works.szabope.plugins.mypy.annotator

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import com.intellij.util.io.delete
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.parser.MypyOutput
import works.szabope.plugins.mypy.toRunConfiguration
import kotlin.io.path.pathString
import kotlin.io.path.writeText

internal class MypyAnnotator : ExternalAnnotator<MypyAnnotator.MypyAnnotatorInfo, List<MypyOutput>>() {

    private val logger = logger<MypyAnnotator>()

    class MypyAnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): MypyAnnotatorInfo {
        return MypyAnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: MypyAnnotatorInfo): List<MypyOutput> {
        val settings = MypySettings.getInstance(info.project)
        settings.ensureValid()
        if (!settings.isComplete()) {
            return emptyList()
        }
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = requireNotNull(fileDocumentManager.getCachedDocument(info.file)) {
            "Please, report this issue at https://github.com/szabope/mypy-pycharm-plugin/issues"
        }

        val tempFile = kotlin.io.path.createTempFile(prefix = "pycharm_mypy_", suffix = "_" + info.file.name)
        try {
            tempFile.toFile().deleteOnExit()
            tempFile.writeText(document.charsSequence)
            val service = MypyService.getInstance(info.project)
            val runConfiguration = MypySettings.getInstance(info.project).toRunConfiguration()
            return service.scan(tempFile.pathString, runConfiguration)
        } finally {
            tempFile.delete()
        }
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyOutput>, holder: AnnotationHolder) {
        logger.debug("Mypy returned ${annotationResult.size} issues for ${file.virtualFile.canonicalPath}")
        val profile = InspectionProjectProfileManager.getInstance(file.project).currentProfile
        val severity = HighlightDisplayKey.findById(MyBundle.message("mypy.inspection.id"))?.let {
            profile.getErrorLevel(it, file).severity
        } ?: HighlightSeverity.ERROR

        annotationResult.forEach { issue ->
            val psiElement = requireNotNull(file.findElementFor(issue)) { "Mypy result mismatch for $issue" }
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
