package works.szabope.plugins.mypy.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.io.delete
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.cli.MypyOutput
import works.szabope.plugins.mypy.toRunConfiguration
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

internal class MypyAnnotator : ExternalAnnotator<MypyAnnotator.MypyAnnotatorInfo, List<MypyOutput>>() {

    class MypyAnnotatorInfo(val file: VirtualFile, val project: Project)

    override fun collectInformation(file: PsiFile): MypyAnnotatorInfo {
        return MypyAnnotatorInfo(file.virtualFile, file.project)
    }

    override fun doAnnotate(info: MypyAnnotatorInfo): List<MypyOutput> {
        if (!MypySettings.getInstance(info.project).isInitialized()) {
            return emptyList()
        }
        val service = MypyService.getInstance(info.project)
        val runConfiguration = MypySettings.getInstance(info.project).toRunConfiguration()
        val scan = fun(absolutePath: String): List<MypyOutput> = service.scan(absolutePath, runConfiguration)
        val content = getCachedContent(info.file) ?: return scan(info.file.path)
        val tempFile = kotlin.io.path.createTempFile("mypy-pycharm-plugin-editor-scan", ".py")
        try {
            tempFile.writeText(content)
            return scan(tempFile.absolutePathString())
        } finally {
            tempFile.delete()
        }
    }

    override fun apply(file: PsiFile, annotationResult: List<MypyOutput>?, holder: AnnotationHolder) {
        if (annotationResult != null) {
            MypyService.getInstance(file.project).annotate(file, annotationResult, holder)
        }
    }

    override fun getPairedBatchInspectionShortName(): String {
        return MyBundle.message("mypy.inspection.id")
    }

    @Suppress("UnstableApiUsage")
    private fun getCachedContent(file: VirtualFile): String? {
        return runBlockingCancellable {
            readAction {
                FileDocumentManager.getInstance().getDocument(file)?.text
            }
        }
    }
}