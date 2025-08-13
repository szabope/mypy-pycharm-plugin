@file:Suppress("removal")

package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.jetbrains.python.packaging.PyExecutionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.dialog.IDialogManager
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel


class InstallMypyAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runWithModalProgressBlocking(project, MyBundle.message("action.InstallMypyAction.in_progress")) {
            withContext(Dispatchers.EDT) {
                MypyPluginPackageManagementService.getInstance(project).installRequirement().onSuccess {
                    @Suppress("DialogTitleCapitalization") ToolWindowManager.getInstance(project).notifyByBalloon(
                        MypyToolWindowPanel.ID, MessageType.INFO, MyBundle.message("action.InstallMypyAction.done_html")
                    )
                    with(OldMypySettings.getInstance(project)) {
                        MypySettings.getInstance(project)
                            .initSettings(customMypyPath, mypyConfigFilePath, mypyArguments)
                    }
                }.onFailure { failure ->
                    if (failure is PyExecutionException) {
                        IDialogManager.showPyPackageInstallationErrorDialog(failure)
                    } else {
                        thisLogger().error(failure)
                        IDialogManager.showGeneralErrorDialog(failure)
                    }
                }
            }
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled =
            event.project?.let { MypyPluginPackageManagementService.getInstance(it).canInstall() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.actions.InstallMypyAction"
    }
}
