package works.szabope.plugins.mypy.annotator

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.SettingsValidator
import works.szabope.plugins.mypy.services.SyncScanService
import works.szabope.plugins.mypy.services.parser.MypyMessage

//TODO: extract to common
class MypyAnnotator : ExternalAnnotator<MypyAnnotator.AnnotatorInfo, Flow<MypyMessage>>() {

    class AnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): AnnotatorInfo {
        return AnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: AnnotatorInfo): Flow<MypyMessage> {
        if (!SettingsValidator(info.project).isComplete(MypySettings.getInstance(info.project).getData())) {
            return flowOf()
        }
        return SyncScanService.getInstance(info.project)
            .scan(listOf(info.file), MypySettings.getInstance(info.project).getData())
    }

    override fun apply(file: PsiFile, annotationResult: Flow<MypyMessage>, holder: AnnotationHolder) {
        val profile = InspectionProjectProfileManager.getInstance(file.project).currentProfile
        val severity = HighlightDisplayKey.findById(MypyInspectionId)?.let {
            profile.getErrorLevel(it, file).severity
        } ?: HighlightSeverity.ERROR
        runBlockingCancellable {
            annotationResult.map {
                it to requireNotNull(file.findElementFor(it)) { "Mypy result mismatch for $it" }
            }.collect { (issue, psiElement) ->
                holder.newAnnotation(severity, issue.message).range(psiElement.textRange)
                    .withFix(MypyIgnoreIntention(issue.line)).create()
            }
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return MypyInspectionId
    }

    private fun PsiFile.findElementFor(issue: MypyMessage): PsiElement? {
        val tabSize = CodeStyle.getFacade(this).tabSize
        val offset = DocumentUtil.calculateOffset(fileDocument, issue.line, issue.column, tabSize)
        return findElementAt(offset)
    }
}
