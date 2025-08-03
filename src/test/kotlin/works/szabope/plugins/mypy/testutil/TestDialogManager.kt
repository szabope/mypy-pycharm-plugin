// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
@file:Suppress("removal")

package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.packaging.PyPackageInstallationErrorDialog
import org.junit.Assert.assertNull
import works.szabope.plugins.mypy.dialog.IDialogManager
import works.szabope.plugins.mypy.dialog.MypyDialog
import works.szabope.plugins.mypy.dialog.MypyExecutionErrorDialog
import works.szabope.plugins.mypy.dialog.MypyGeneralErrorDialog

class TestDialogManager : IDialogManager {
    private val myHandlers = hashMapOf<Class<out DialogWrapper>, (MypyDialog) -> Int>()

    override fun showDialog(dialog: MypyDialog) {
        dialog.show()
        var exitCode = DialogWrapper.OK_EXIT_CODE
        try {
            val handler = myHandlers[dialog.getWrappedClass()]
            if (handler != null) {
                exitCode = handler(dialog)
            } else {
                throw IllegalStateException("The dialog is not expected here: " + dialog.javaClass)
            }
        } finally {
            dialog.close(exitCode)
        }
    }

    override fun createPyPackageInstallationErrorDialog(exception: PyExecutionException) =
        TestDialogWrapper(PyPackageInstallationErrorDialog::class.java)

    override fun createMypyExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        TestDialogWrapper(MypyExecutionErrorDialog::class.java)

    override fun createGeneralErrorDialog(failure: Throwable) =
        TestDialogWrapper(MypyGeneralErrorDialog::class.java, failure)

    override fun onDialog(dialogClass: Class<out DialogWrapper>, handler: (MypyDialog) -> Int) {
        assertNull(myHandlers.put(dialogClass, handler))
    }

    override fun cleanup() {
        myHandlers.clear()
    }
}