package works.szabope.plugins.mypy.action

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.action.AbstractInstallToolAction
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

class InstallMypyAction : AbstractInstallToolAction(MypyBundle.message("action.InstallMypyAction.done_html")) {
    override val toolWindowId = MypyToolWindowPanel.ID
    override fun getPackageManager(project: Project) = MypyPluginPackageManagementService.getInstance(project)

    override fun handleFailure(failure: Throwable) {
        when (failure) {
            is PluginPackageManagementException.InstallationFailedException -> {
                DialogManager.showPyPackageInstallationErrorDialog(failure)
            }

            else -> {
                thisLogger().error(failure)
                DialogManager.showGeneralErrorDialog(failure)
            }
        }
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.InstallMypyAction"
    }
}
