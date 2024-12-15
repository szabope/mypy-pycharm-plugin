@file:Suppress("removal")

package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.ui.PyPackageManagementService.PyPackageInstallationErrorDescription
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.TestOnly

interface MypyDialog {
    fun getWrappedClass(): Class<out DialogWrapper>
    fun show()
    fun close(exitCode: Int)
    @TestOnly
    fun isShown(): Boolean? = null
    @TestOnly
    fun getExitCode(): Int? = null
}

interface IDialogManager {
    fun showDialog(dialog: MypyDialog)

    fun createPyPackageInstallationErrorDialog(
        @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
    ): MypyDialog

    fun createPackagingErrorDialog(
        @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
    ): MypyDialog

    fun createMypyExecutionErrorDialog(command: String, result: String, resultCode: Int): MypyDialog

    @TestOnly
    fun onDialog(dialogClass: Class<out DialogWrapper>, handler: (MypyDialog) -> Int) = Unit

    @TestOnly
    fun cleanup() = Unit

    companion object {
        fun showPyPackageInstallationErrorDialog(
            @Nls title: String, errorDescription: PyPackageInstallationErrorDescription
        ) = with(dialogManager()) {
            val dialog = createPyPackageInstallationErrorDialog(title, errorDescription)
            showDialog(dialog)
        }

        fun showPackagingErrorDialog(
            @Nls title: String, errorDescription: PackageManagementService.ErrorDescription
        ) = with(dialogManager()) {
            val dialog = createPackagingErrorDialog(title, errorDescription)
            showDialog(dialog)
        }

        fun showMypyExecutionErrorDialog(command: String, result: String, resultCode: Int) = with(dialogManager()) {
            val dialog = createMypyExecutionErrorDialog(command, result, resultCode)
            showDialog(dialog)
        }

        private fun dialogManager(): IDialogManager {
            return service<IDialogManager>()
        }
    }
}