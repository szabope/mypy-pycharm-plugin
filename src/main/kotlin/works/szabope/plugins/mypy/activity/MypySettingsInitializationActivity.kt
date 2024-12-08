package works.szabope.plugins.mypy.activity

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.EntityChange
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypyPackageManagerService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

internal class MypySettingsInitializationActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        configureMypy(project)
        project.workspaceModel.eventLog.onEach {
            val moduleChanges = it.getChanges(ModuleEntity::class.java)
            if (moduleChanges.filterIsInstance<EntityChange.Replaced<ModuleEntity>>().isNotEmpty()) {
                configureMypy(project)
            }
        }.collect()
    }

    private suspend fun configureMypy(project: Project) {
        val settings = MypySettings.getInstance(project)
        if (!settings.isComplete()) {
            with(OldMypySettings.getInstance(project)) {
                settings.initSettings(customMypyPath, mypyConfigFilePath, mypyArguments)
            }
        }
        if (!settings.isComplete()) {
            notifyIncompleteConfiguration(project)
        }
    }

    private fun notifyIncompleteConfiguration(project: Project) {
        val notification = NotificationGroupManager.getInstance().getNotificationGroup("Mypy Group")
            .createNotification(MyBundle.message("mypy.settings.incomplete"), NotificationType.WARNING)
        val openSettingsAction = ActionManager.getInstance().getAction("MyPyOpenSettingsAction")
        notification.addAction(
            NotificationAction.create(
                MyBundle.message("mypy.intention.complete_configuration.text")
            ) { event, _ ->
                run {
                    ActionUtil.performActionDumbAwareWithCallbacks(openSettingsAction, event)
                    notification.hideBalloon()
                }
            })
        if (MypyPackageManagerService.getInstance(project).canInstall()) {
            val installMypyAction = ActionManager.getInstance().getAction("InstallMypyAction")
            notification.addAction(
                NotificationAction.create(
                    MyBundle.message("mypy.intention.install_mypy.text"),
                ) { event, _ ->
                    run {
                        ActionUtil.performActionDumbAwareWithCallbacks(installMypyAction, event)
                        notification.expire()
                    }
                })
        }
        notification.notify(project)
    }
}
