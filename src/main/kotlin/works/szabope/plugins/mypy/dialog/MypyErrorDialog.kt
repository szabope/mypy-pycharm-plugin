package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.generator.nova.GenerationSpec.Companion.nullIfEmpty
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.mypy.MypyBundle

data class MypyErrorDescription(
    @DetailedDescription val details: String?, @DetailedDescription val message: String? = null
)

class MypyPackageInstallationErrorDialog(message: String) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.installation_error.title"),
    MypyErrorDescription(message, MypyBundle.message("mypy.dialog.installation_error.message"))
)

class FailedToExecuteErrorDialog(message: String) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.failed_to_execute.title"), MypyErrorDescription(
        message, MypyBundle.message("mypy.dialog.failed_to_execute.message")
    )
)

class MypyExecutionErrorDialog(
    configuration: ImmutableSettingsData, result: String, resultCode: Int?
) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.execution_error.title"), MypyErrorDescription(
        MypyBundle.message("mypy.dialog.execution_error.content", configuration, result),
        resultCode?.let { MypyBundle.message("mypy.dialog.execution_error.status_code", it) })
)

class MypyParseErrorDialog(
    configuration: ImmutableSettingsData, targets: String, json: String, error: String
) : MypyErrorDialog(
    MypyBundle.message("mypy.dialog.parse_error.title"), MypyErrorDescription(
        MypyBundle.message("mypy.dialog.parse_error.details", configuration, targets, json),
        error.nullIfEmpty()?.let { MypyBundle.message("mypy.dialog.parse_error.message", it) })
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
        setTitle(title)
        super.init()
        setErrorText(description.message)
    }

    override fun createCenterPanel() = description.details?.let { details ->
        panel {
            row {
                textArea().applyToComponent {
                    text = details
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    setSize(JBUI.scale(800), 0)
                }.align(Align.FILL)
            }
        }.withPreferredSize(JBUI.scale(800), 0)
    }
}
