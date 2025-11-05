package works.szabope.plugins.mypy.annotator

import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.jetbrains.python.inspections.PyInspection

const val MypyInspectionId = "MypyInspection"

internal class MypyInspection : PyInspection(), ExternalAnnotatorBatchInspection {

    override fun getShortName(): String {
        return MypyInspectionId
    }
}
