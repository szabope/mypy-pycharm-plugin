package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.PyPackagesNotificationPanel
import com.jetbrains.python.packaging.bridge.PythonPackageManagementServiceBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyPackageUtil
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import java.util.concurrent.CompletableFuture

class InstallMypyAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val packageManager = MypyPackageUtil.getPackageManager(project)!!
        runWithModalProgressBlocking(project, MyBundle.message("action.InstallMypyAction.in_progress")) {
            withContext(Dispatchers.EDT) {
                val errorDescription = install(packageManager)
                if (errorDescription == null) {
                    @Suppress("DialogTitleCapitalization") ToolWindowManager.getInstance(project).notifyByBalloon(
                        MypyToolWindowPanel.ID, MessageType.INFO, MyBundle.message("action.InstallMypyAction.done_html")
                    )
                    with(OldMypySettings.getInstance(project)) {
                        MypySettings.getInstance(project)
                            .initSettings(customMypyPath, mypyConfigFilePath, mypyArguments)
                    }
                } else {
                    @Suppress("DialogTitleCapitalization") PyPackagesNotificationPanel.showPackageInstallationError(
                        MyBundle.message("action.InstallMypyAction.fail_html"), errorDescription
                    )
                }
            }
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { MypyPackageUtil.canInstall(it) } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private suspend fun install(packageManager: PythonPackageManagementServiceBridge): PackageManagementService.ErrorDescription? {
        val result = CompletableFuture<PackageManagementService.ErrorDescription>()
        val listener = object : PackageManagementService.Listener {
            override fun operationStarted(packageName: String?) = Unit
            override fun operationFinished(ignored: String?, error: PackageManagementService.ErrorDescription?) {
                if (error == null) {
                    result.complete(null)
                } else {
                    result.complete(error)
                }
            }
        }
        packageManager.installPackage(MypyPackageUtil.getPackage(), null, false, null, listener, false)
        return result.await()
    }
}
