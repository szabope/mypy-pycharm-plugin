// inspired by idea/243.19420.21 git4idea.DialogManager
package works.szabope.plugins.mypy.dialog

import works.szabope.plugins.common.dialog.AbstractDialogManager
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.PluginDialog
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.common.services.ToolExecutorConfiguration

class DialogManager : AbstractDialogManager() {

    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        MypyPackageInstallationErrorDialog(exception.message).asPluginDialog()

    override fun createToolExecutionErrorDialog(
        configuration: ToolExecutorConfiguration,
        result: String,
        resultCode: Int
    ) = MypyExecutionErrorDialog(configuration, result, resultCode).asPluginDialog()

    override fun createToolOutputParseErrorDialog(
        configuration: ToolExecutorConfiguration,
        targets: String,
        json: String,
        error: String
    ) = MypyParseErrorDialog(configuration, targets, json, error).asPluginDialog()

    override fun createGeneralErrorDialog(failure: Throwable) = MypyGeneralErrorDialog(failure).asPluginDialog()

    companion object : IDialogManager {
        var dialogManager: IDialogManager = DialogManager()

        override fun showDialog(dialog: PluginDialog) = dialogManager.showDialog(dialog)
        override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
            dialogManager.createPyPackageInstallationErrorDialog(exception)
        override fun createToolExecutionErrorDialog(configuration: ToolExecutorConfiguration, result: String, resultCode: Int) =
            dialogManager.createToolExecutionErrorDialog(configuration, result, resultCode)
        override fun createToolOutputParseErrorDialog(configuration: ToolExecutorConfiguration, targets: String, json: String, error: String) =
            dialogManager.createToolOutputParseErrorDialog(configuration, targets, json, error)
        override fun createGeneralErrorDialog(failure: Throwable) =
            dialogManager.createGeneralErrorDialog(failure)
    }
}