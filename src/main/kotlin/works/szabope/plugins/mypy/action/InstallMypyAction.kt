package works.szabope.plugins.mypy.action

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.common.action.AbstractInstallToolAction
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

class InstallMypyAction : AbstractInstallToolAction(MypyBundle.message("action.InstallMypyAction.done_html")) {
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

    override fun notifyPanel(project: Project, message: String) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
            MypyToolWindowPanel.ID, MessageType.INFO, message
        )
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.InstallMypyAction"
    }
}
