// inspired by idea/243.19420.21 git4idea.DialogManager
package works.szabope.plugins.mypy.dialog

import works.szabope.plugins.common.dialog.AbstractDialogManager
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.PluginDialog
import works.szabope.plugins.common.services.PluginPackageManagementException

class DialogManager : AbstractDialogManager() {

    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        MypyPackageInstallationErrorDialog(exception.message).asPluginDialog()

    override fun createToolExecutionErrorDialog(
        commandLine: String,
        result: String,
        resultCode: Int?
    ) = MypyExecutionErrorDialog(commandLine, result, resultCode).asPluginDialog()

    override fun createToolOutputParseErrorDialog(
        commandLine: String,
        targets: String,
        json: String,
        error: String
    ) = MypyParseErrorDialog(commandLine, targets, json, error).asPluginDialog()

    override fun createGeneralErrorDialog(failure: Throwable) = MypyGeneralErrorDialog(failure).asPluginDialog()

    companion object : IDialogManager {
        var dialogManager: IDialogManager = DialogManager()

        override fun showDialog(dialog: PluginDialog) = dialogManager.showDialog(dialog)
        override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
            dialogManager.createPyPackageInstallationErrorDialog(exception)
        override fun createToolExecutionErrorDialog(commandLine: String, result: String, resultCode: Int?) =
            dialogManager.createToolExecutionErrorDialog(commandLine, result, resultCode)
        override fun createToolOutputParseErrorDialog(commandLine: String, targets: String, json: String, error: String) =
            dialogManager.createToolOutputParseErrorDialog(commandLine, targets, json, error)
        override fun createGeneralErrorDialog(failure: Throwable) =
            dialogManager.createGeneralErrorDialog(failure)
    }
}