// inspired by idea/243.19420.21 git4idea.DialogManager
@file:Suppress("removal")

package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.PackagingErrorDialog
import com.jetbrains.python.packaging.PyPackageInstallationErrorDialog
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import org.jetbrains.annotations.Nls

private fun DialogWrapper.toMypyDialog() = object : MypyDialog {
    override fun getWrappedClass(): Class<out DialogWrapper> = this@toMypyDialog::class.java
    override fun show() = this@toMypyDialog.show()
    override fun close(exitCode: Int) = this@toMypyDialog.close(exitCode)
}

class DialogManager : IDialogManager {
    override fun showDialog(dialog: MypyDialog) = dialog.show()

    override fun createPyPackageInstallationErrorDialog(
        @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
    ) = PyPackageInstallationErrorDialog(title, errorDescription).toMypyDialog()

    override fun createPackagingErrorDialog(
        @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
    ) = PackagingErrorDialog(title, errorDescription).toMypyDialog()

    override fun createMypyExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        MypyExecutionErrorDialog(command, result, resultCode).toMypyDialog()
}
