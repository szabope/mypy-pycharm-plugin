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
    @DetailedDescription val details: String, @DetailedDescription val message: String? = null
)

class MypyPackageInstallationErrorDialog(message: String) : MypyErrorDialog(
    MyBundle.message("mypy.dialog.installation_error.title"),
    MypyErrorDescription(MyBundle.message("mypy.dialog.installation_error.details", message))
)


class MypyExecutionErrorDialog(command: String, result: String, resultCode: Int) : MypyErrorDialog(
    MyBundle.message("mypy.dialog.execution_error.title"), MypyErrorDescription(
        MyBundle.message("mypy.dialog.execution_error.status_code", resultCode, result),
        MyBundle.message("mypy.dialog.execution_error.message", command)
    )
)

class MypyGeneralErrorDialog(throwable: Throwable) : MypyErrorDialog(
    MyBundle.message("mypy.dialog.general_error.title"), MypyErrorDescription(
        MyBundle.message(
            "mypy.dialog.general_error.details", throwable.message!!, throwable.stackTraceToString()
        ), MyBundle.message("mypy.please_report_this_issue")
    )
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
                text = description.details
                isEditable = false
                columns = COLUMNS_LARGE
                lineWrap = true
            }.align(Align.FILL)
        }.layout(RowLayout.PARENT_GRID)
    }
}
