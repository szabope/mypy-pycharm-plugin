@file:Suppress("removal")

package works.szabope.plugins.mypy.dialog

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.python.packaging.PyExecutionException
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

    fun createPyPackageInstallationErrorDialog(exception: PyExecutionException): MypyDialog

    fun createMypyExecutionErrorDialog(command: String, result: String, resultCode: Int): MypyDialog

    fun createGeneralErrorDialog(failure: Throwable): MypyDialog

    @TestOnly
    fun onDialog(dialogClass: Class<out DialogWrapper>, handler: (MypyDialog) -> Int) = Unit

    @TestOnly
    fun cleanup() = Unit

    companion object {
        fun showPyPackageInstallationErrorDialog(
            exception: PyExecutionException
        ) = with(dialogManager()) {
            val dialog = createPyPackageInstallationErrorDialog(exception)
            showDialog(dialog)
        }

        fun showMypyExecutionErrorDialog(command: String, result: String, resultCode: Int) = with(dialogManager()) {
            val dialog = createMypyExecutionErrorDialog(command, result, resultCode)
            showDialog(dialog)
        }

        fun showGeneralErrorDialog(failure: Throwable) = with(dialogManager()) {
            val dialog = createGeneralErrorDialog(failure)
            showDialog(dialog)
        }

        private fun dialogManager(): IDialogManager {
            return service<IDialogManager>()
        }
    }
}