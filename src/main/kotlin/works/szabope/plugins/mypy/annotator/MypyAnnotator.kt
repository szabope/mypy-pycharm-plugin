package works.szabope.plugins.mypy.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.cli.MypyOutput
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.toRunConfiguration

internal class MypyAnnotator : ExternalAnnotator<MypyAnnotator.MypyAnnotatorInfo, List<MypyOutput>>() {

    class MypyAnnotatorInfo(val filePath: String, val project: Project)

    override fun collectInformation(file: PsiFile): MypyAnnotatorInfo {
        return MypyAnnotatorInfo(file.virtualFile.path, file.project)
    }

    override fun doAnnotate(info: MypyAnnotatorInfo): List<MypyOutput> {
        if (MypySettings.getInstance(info.project).isInitialized()) {
            return MypyService.getInstance(info.project)
                .scan(info.filePath, MypySettings.getInstance(info.project).toRunConfiguration())
        }
        return emptyList()
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyOutput>?, holder: AnnotationHolder) {
        if (annotationResult != null) {
            MypyService.getInstance(file.project).annotate(file, annotationResult, holder)
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return MyBundle.message("mypy.inspection.id")
    }
}