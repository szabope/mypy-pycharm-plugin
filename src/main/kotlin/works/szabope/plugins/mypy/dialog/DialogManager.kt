// inspired by idea/243.19420.21 git4idea.DialogManager
@file:Suppress("removal")

package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.ui.DialogWrapper
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.IDialogManager.IShowDialog
import works.szabope.plugins.common.dialog.PluginDialog
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.PluginPackageManagementException

private fun DialogWrapper.toMypyDialog() = object : PluginDialog {
    override fun show() = this@toMypyDialog.show()
}

class DialogManager : IDialogManager {
    override fun showDialog(dialog: PluginDialog) = dialog.show()

    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        MypyPackageInstallationErrorDialog(exception.message).toMypyDialog()

    override fun createToolExecutionErrorDialog(
        configuration: ImmutableSettingsData,
        result: String,
        resultCode: Int
    ) = MypyExecutionErrorDialog(configuration, result, resultCode).toMypyDialog()

    override fun createFailedToExecuteErrorDialog(message: String) =
        FailedToExecuteErrorDialog(message).toMypyDialog()

    override fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData,
        targets: String,
        json: String,
        error: String
    ) = MypyParseErrorDialog(configuration, targets, json, error).toMypyDialog()

    override fun createGeneralErrorDialog(failure: Throwable) = MypyGeneralErrorDialog(failure).toMypyDialog()

    companion object : IShowDialog {
        override val dialogManager: IDialogManager by lazy { DialogManager() }
    }
}
