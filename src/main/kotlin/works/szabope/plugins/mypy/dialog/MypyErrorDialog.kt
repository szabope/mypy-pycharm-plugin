package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import works.szabope.plugins.mypy.MyBundle

data class MypyErrorDescription(
    val command: String, val output: String, @DetailedDescription val message: String? = null
)

class MypyExecutionErrorDialog(command: String, result: String, resultCode: Int) : MypyErrorDialog(
    MyBundle.message("mypy.dialog.execution_error.title"),
    MypyErrorDescription(command, result, MyBundle.message("mypy.dialog.execution_error.status_code", resultCode))
)

open class MypyErrorDialog(
    title: @DialogTitle String, private val description: MypyErrorDescription
) : DialogWrapper(false) {

    init {
        super.init()
        super.setTitle(title)
        super.setErrorText(description.message)
    }

    override fun createCenterPanel() = panel {
        row {
            textArea().applyToComponent {
                text = MyBundle.message("mypy.dialog.execution_error.content", description.command, description.output)
                isEditable = false
                columns = COLUMNS_LARGE
                lineWrap = true
            }.align(Align.FILL)
        }.layout(RowLayout.PARENT_GRID)
    }
}
