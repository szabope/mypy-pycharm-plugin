// inspired by idea/243.19420.21 git4idea.DialogManager
@file:Suppress("removal")

package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.python.packaging.PyExecutionException

private fun DialogWrapper.toMypyDialog() = object : MypyDialog {
    override fun getWrappedClass(): Class<out DialogWrapper> = this@toMypyDialog::class.java
    override fun show() = this@toMypyDialog.show()
    override fun close(exitCode: Int) = this@toMypyDialog.close(exitCode)
}

class DialogManager : IDialogManager {
    override fun showDialog(dialog: MypyDialog) = dialog.show()

    override fun createPyPackageInstallationErrorDialog(exception: PyExecutionException) =
        MypyPackageInstallationErrorDialog(exception.message!!).toMypyDialog()

    override fun createMypyExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        MypyExecutionErrorDialog(command, result, resultCode).toMypyDialog()

    override fun createGeneralErrorDialog(failure: Throwable) = MypyGeneralErrorDialog(failure).toMypyDialog()
}
