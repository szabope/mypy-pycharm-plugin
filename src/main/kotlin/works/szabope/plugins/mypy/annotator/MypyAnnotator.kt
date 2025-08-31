package works.szabope.plugins.mypy.annotator

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import com.intellij.util.io.delete
import works.szabope.plugins.common.services.tool.CollectingToolOutputHandler
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.SettingsValidator
import works.szabope.plugins.mypy.services.SyncScanService
import works.szabope.plugins.mypy.services.parser.MypyMessage
import kotlin.io.path.writeText

//TODO: extract to common
class MypyAnnotator : ExternalAnnotator<MypyAnnotator.AnnotatorInfo, List<MypyMessage>>() {

    class AnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): AnnotatorInfo {
        return AnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: AnnotatorInfo): List<MypyMessage> {
        val settings = MypySettings.getInstance(info.project)
        if (!SettingsValidator(info.project).isComplete(settings.getData())) {
            return emptyList()
        }
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = requireNotNull(fileDocumentManager.getCachedDocument(info.file)) {
            MypyBundle.message("mypy.please_report_this_issue")
        }
        val tempFile = kotlin.io.path.createTempFile(prefix = "pycharm_mypy_", suffix = ".py")
        try {
            tempFile.toFile().deleteOnExit()
            tempFile.writeText(document.charsSequence)
            val virtualTempFile =
                requireNotNull(VirtualFileManager.getInstance().refreshAndFindFileByNioPath(tempFile)) {
                    "Could not find virtual file at ${tempFile.toCanonicalPath()}"
                }
            val resultHandler = CollectingToolOutputHandler<MypyMessage>()
            SyncScanService.getInstance(info.project).scan(listOf(virtualTempFile), settings.getData(), resultHandler)
            return resultHandler.getResults()
        } finally {
            tempFile.delete()
        }
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyMessage>, holder: AnnotationHolder) {
        thisLogger().debug("Mypy returned ${annotationResult.size} issues for ${file.virtualFile.canonicalPath}")
        val profile = InspectionProjectProfileManager.getInstance(file.project).currentProfile
        val severity = HighlightDisplayKey.findById(MypyBundle.message("mypy.inspection.id"))?.let {
            profile.getErrorLevel(it, file).severity
        } ?: HighlightSeverity.ERROR

        annotationResult.forEach { issue ->
            val psiElement = requireNotNull(file.findElementFor(issue)) { "Mypy result mismatch for $issue" }
            holder.newAnnotation(severity, issue.message).range(psiElement.textRange)
                .withFix(MypyIgnoreIntention(issue.line)).create()
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return MypyBundle.message("mypy.inspection.id")
    }

    private fun PsiFile.findElementFor(issue: MypyMessage): PsiElement? {
        val tabSize = CodeStyle.getFacade(this).tabSize
        val offset = DocumentUtil.calculateOffset(fileDocument, issue.line, issue.column, tabSize)
        return findElementAt(offset)
    }
}
