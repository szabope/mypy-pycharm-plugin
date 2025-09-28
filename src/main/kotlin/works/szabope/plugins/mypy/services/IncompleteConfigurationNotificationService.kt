package works.szabope.plugins.mypy.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.action.InstallMypyAction
import works.szabope.plugins.mypy.action.OpenSettingsAction
import java.lang.ref.WeakReference

@Service(Service.Level.PROJECT)
class IncompleteConfigurationNotificationService(private val project: Project) {

    private var notification: WeakReference<Notification> = WeakReference(null)

    @Synchronized
    fun notify(canInstall: Boolean) {
        val notification =
            NotificationGroupManager.getInstance().getNotificationGroup(MypyBundle.message("notification.group.mypy.group"))
                .createNotification(
                    MypyBundle.message("mypy.notification.incomplete_configuration"), NotificationType.WARNING
                )
        val openSettingsAction = ActionManager.getInstance().getAction(OpenSettingsAction.ID)
        notification.addAction(
            NotificationAction.create(
                MypyBundle.message("mypy.intention.complete_configuration.text")
            ) { event, _ ->
                run {
                    ActionUtil.performAction(openSettingsAction, event)
                    notification.hideBalloon()
                }
            })
        if (canInstall) {
            val installAction = ActionManager.getInstance().getAction(InstallMypyAction.ID)
            notification.addAction(
                NotificationAction.create(
                    MypyBundle.message("mypy.intention.install_mypy.text"),
                ) { event, _ ->
                    run {
                        ActionUtil.performAction(installAction, event)
                        notification.expire()
                    }
                })
        }
        this.notification.get()?.expire()
        this.notification = WeakReference(notification)
        notification.notify(project)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): IncompleteConfigurationNotificationService = project.service()
    }
}