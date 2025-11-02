package works.szabope.plugins.mypy.annotator

import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.jetbrains.python.inspections.PyInspection
import works.szabope.plugins.mypy.MypyBundle

internal class MypyInspection : PyInspection(), ExternalAnnotatorBatchInspection {

    override fun getShortName(): String {
        return MypyBundle.message("mypy.inspection.id")
    }
}
