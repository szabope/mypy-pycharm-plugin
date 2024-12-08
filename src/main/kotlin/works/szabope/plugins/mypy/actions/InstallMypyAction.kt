package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.PyPackagesNotificationPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyPackageManagerService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

class InstallMypyAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        MypyPackageManagerService.getInstance(project).install(getSuccessHandler(project), this::handleFailure)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { ProjectRootManager.getInstance(it).projectSdk } != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun getSuccessHandler(project: Project) = suspend {
        @Suppress("DialogTitleCapitalization") ToolWindowManager.getInstance(project)
            .notifyByBalloon(
                MypyToolWindowPanel.ID,
                MessageType.INFO,
                MyBundle.message("action.InstallMypyAction.done_html")
            )
        with(OldMypySettings.getInstance(project)) {
            MypySettings.getInstance(project).initSettings(customMypyPath, mypyConfigFilePath, mypyArguments)
        }
    }

    private suspend fun handleFailure(description: PackageManagementService.ErrorDescription) {
        withContext(Dispatchers.EDT) {
            @Suppress("DialogTitleCapitalization")
            PyPackagesNotificationPanel.showPackageInstallationError(
                MyBundle.message("action.InstallMypyAction.fail_html"),
                description
            )
        }
    }
}
