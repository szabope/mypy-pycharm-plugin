package works.szabope.plugins.mypy.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.action.InstallMypyAction
import works.szabope.plugins.mypy.action.OpenSettingsAction

class IncompleteConfigurationNotifier {
    companion object {
        @JvmStatic
        fun notify(project: Project, canInstall: Boolean) {
            val openSettingsAction = ActionManager.getInstance().getAction(OpenSettingsAction.ID)
            val notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup(MypyBundle.message("notification.group.mypy.group"))
            val notification = notificationGroup.createNotification(
                MypyBundle.message("mypy.notification.incomplete_configuration"), NotificationType.WARNING
            ).addAction(openSettingsAction)
            if (canInstall) {
                val installAction = ActionManager.getInstance().getAction(InstallMypyAction.ID)
                notification.addAction(installAction)
            }
            notification.notify(project)
        }
    }
}
