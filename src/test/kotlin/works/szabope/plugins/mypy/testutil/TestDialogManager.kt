// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
package works.szabope.plugins.mypy.testutil

import works.szabope.plugins.common.services.ToolExecutorConfiguration
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.common.test.dialog.AbstractTestDialogManager
import works.szabope.plugins.common.test.dialog.TestDialogWrapper
import works.szabope.plugins.mypy.dialog.*

class TestDialogManager : AbstractTestDialogManager() {
    override fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        TestDialogWrapper(
            MypyPackageInstallationErrorDialog::class.java, exception
        )

    override fun createToolExecutionErrorDialog(configuration: ToolExecutorConfiguration, result: String, resultCode: Int) =
        TestDialogWrapper(MypyExecutionErrorDialog::class.java, configuration, result, resultCode)

    override fun createToolOutputParseErrorDialog(
        configuration: ToolExecutorConfiguration, targets: String, json: String, error: String
    ) = TestDialogWrapper(MypyParseErrorDialog::class.java, configuration, targets, json, error)

    override fun createGeneralErrorDialog(failure: Throwable) =
        TestDialogWrapper(MypyGeneralErrorDialog::class.java, failure)
}
