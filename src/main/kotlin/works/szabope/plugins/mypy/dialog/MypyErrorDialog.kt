package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle

data class MypyErrorDescription(
    @DetailedDescription val details: String, @DetailedDescription val message: String? = null
)

class MypyPackageInstallationErrorDialog(message: String) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.installation_error.title"),
    MypyErrorDescription(MypyBundle.message("mypy.dialog.installation_error.details", message))
)


class MypyExecutionErrorDialog(configuration: ImmutableSettingsData, result: String, resultCode: Int) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.execution_error.title"), MypyErrorDescription(
        MypyBundle.message("mypy.dialog.execution_error.content", configuration, result),
        MypyBundle.message("mypy.dialog.execution_error.status_code", resultCode)
    )
)

class MypyParseErrorDialog(configuration: ImmutableSettingsData, targets: String, json: String, error: String) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.parse_error.title"), MypyErrorDescription(
        MypyBundle.message("mypy.dialog.parse_error.details", configuration, targets, json),
        MypyBundle.message("mypy.dialog.parse_error.message", error)
    )
)

class MypyGeneralErrorDialog(throwable: Throwable) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.general_error.title"), MypyErrorDescription(
        MypyBundle.message(
            "mypy.dialog.general_error.details", throwable.message!!, throwable.stackTraceToString()
        ), MypyBundle.message("mypy.please_report_this_issue")
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
