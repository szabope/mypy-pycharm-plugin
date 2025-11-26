package works.szabope.plugins.mypy.annotator

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.DocumentUtil
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.SyncScanService
import works.szabope.plugins.mypy.services.parser.MypyMessage
import kotlin.time.measureTimedValue

//TODO: extract to common
class MypyAnnotator : ExternalAnnotator<MypyAnnotator.AnnotatorInfo, List<MypyMessage>>() {

    class AnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): AnnotatorInfo? {
        // do not run for in-memory files, shadowing would fail
        if (file.virtualFile is LightVirtualFile) return null
        return AnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: AnnotatorInfo): List<MypyMessage> {
        val configuration = MypySettings.getInstance(info.project).getValidConfiguration()
        if (configuration.isFailure) {
            return emptyList()
        }
        return SyncScanService.getInstance(info.project).scan(listOf(info.file), configuration.getOrThrow())
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyMessage>, holder: AnnotationHolder) {
        val profile = InspectionProjectProfileManager.getInstance(file.project).currentProfile
        val severity = HighlightDisplayKey.findById(MypyInspectionId)?.let {
            profile.getErrorLevel(it, file).severity
        } ?: HighlightSeverity.ERROR
        annotationResult.map {
            it to requireNotNull(file.findElementFor(it)) { "Mypy result mismatch for $it" }
        }.forEach { (issue, psiElement) ->
            holder.newAnnotation(severity, issue.message).range(psiElement.textRange)
                .withFix(MypyIgnoreIntention(issue)).create()
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return MypyInspectionId
    }

    private fun PsiFile.findElementFor(issue: MypyMessage): PsiElement? {
        val (result, duration) = measureTimedValue {
            val tabSize = CodeStyle.getFacade(this).tabSize
            val offset = DocumentUtil.calculateOffset(fileDocument, issue.line, issue.column, tabSize)
            findElementAt(offset)
        }
        thisLogger().debug($$"MypyAnnotator$PsiFile#findElementFor took $$duration")
        return result
    }
}
