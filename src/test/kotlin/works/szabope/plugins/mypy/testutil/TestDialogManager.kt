// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
@file:Suppress("removal")

package works.szabope.plugins.mypy.testutil

import com.jetbrains.python.packaging.PyExecutionException
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.test.dialog.AbstractTestDialogManager
import works.szabope.plugins.common.test.dialog.TestDialogWrapper
import works.szabope.plugins.mypy.dialog.MypyExecutionErrorDialog
import works.szabope.plugins.mypy.dialog.MypyGeneralErrorDialog
import works.szabope.plugins.mypy.dialog.MypyPackageInstallationErrorDialog
import works.szabope.plugins.mypy.dialog.MypyParseErrorDialog

class TestDialogManager : AbstractTestDialogManager() {
    override fun createPyPackageInstallationErrorDialog(exception: PyExecutionException) = TestDialogWrapper(
        MypyPackageInstallationErrorDialog::class.java, exception
    )

    override fun createToolExecutionErrorDialog(configuration: ImmutableSettingsData, result: String, resultCode: Int) =
        TestDialogWrapper(MypyExecutionErrorDialog::class.java, configuration, result, resultCode)

    override fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData, targets: String, json: String, error: String
    ) = TestDialogWrapper(MypyParseErrorDialog::class.java, configuration, targets, json, error)

    override fun createGeneralErrorDialog(failure: Throwable) =
        TestDialogWrapper(MypyGeneralErrorDialog::class.java, failure)
}
