package works.szabope.plugins.mypy.dialog

import works.szabope.plugins.common.dialog.PluginErrorDescription
import works.szabope.plugins.common.dialog.PluginErrorDialog
import works.szabope.plugins.mypy.MypyBundle

class MypyPackageInstallationErrorDialog(message: String) : PluginErrorDialog(
    MypyBundle.message("mypy.dialog.installation_error.title"),
    PluginErrorDescription(message, MypyBundle.message("mypy.dialog.installation_error.message"))
)

class MypyExecutionErrorDialog(
    commandLine: String, result: String, resultCode: Int?
) : PluginErrorDialog(
    MypyBundle.message("mypy.dialog.execution_error.title"), PluginErrorDescription(
        MypyBundle.message("mypy.dialog.execution_error.content", commandLine, result),
        resultCode?.let { MypyBundle.message("mypy.dialog.execution_error.status_code", it) })
)

class MypyParseErrorDialog(
    commandLine: String, targets: String, json: String, error: String
) : PluginErrorDialog(
    MypyBundle.message("mypy.dialog.parse_error.title"), PluginErrorDescription(
        MypyBundle.message("mypy.dialog.parse_error.details", commandLine, targets, json),
        error.ifEmpty { null }?.let { MypyBundle.message("mypy.dialog.parse_error.message", it) })
)

class MypyGeneralErrorDialog(throwable: Throwable) : PluginErrorDialog(
    MypyBundle.message("mypy.dialog.general_error.title"), PluginErrorDescription(
        MypyBundle.message(
            "mypy.dialog.general_error.details", throwable.message ?: throwable.toString(), throwable.stackTraceToString()
        ), MypyBundle.message("mypy.please_report_this_issue")
    )
)
