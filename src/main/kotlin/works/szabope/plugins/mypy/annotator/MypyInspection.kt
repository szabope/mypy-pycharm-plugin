package works.szabope.plugins.mypy.annotator

import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.jetbrains.python.inspections.PyInspection
import works.szabope.plugins.mypy.MyBundle

internal class MypyInspection : PyInspection(), ExternalAnnotatorBatchInspection {

    override fun getShortName(): String {
        return MyBundle.message("mypy.inspection.id")
    }
}
