// inspired by idea/243.19420.21 git4idea.DialogManager
package works.szabope.plugins.mypy.dialog

import works.szabope.plugins.common.dialog.AbstractDialogManager
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.IDialogManager.IShowDialog
import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.PluginPackageManagementException

class DialogManager : AbstractDialogManager() {

    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        MypyPackageInstallationErrorDialog(exception.message).asPluginDialog()

    override fun createToolExecutionErrorDialog(
        configuration: ToolExecutorConfiguration,
        result: String,
        resultCode: Int
    ) = MypyExecutionErrorDialog(configuration, result, resultCode).asPluginDialog()

    override fun createFailedToExecuteErrorDialog(message: String) =
        FailedToExecuteErrorDialog(message).asPluginDialog()

    override fun createToolOutputParseErrorDialog(
        configuration: ToolExecutorConfiguration,
        targets: String,
        json: String,
        error: String
    ) = MypyParseErrorDialog(configuration, targets, json, error).asPluginDialog()

    override fun createGeneralErrorDialog(failure: Throwable) = MypyGeneralErrorDialog(failure).asPluginDialog()

    companion object : IShowDialog {
        override val dialogManager: IDialogManager by lazy { DialogManager() }
    }
}
