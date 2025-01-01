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
import works.szabope.plugins.mypy.MyBundle
import java.lang.ref.WeakReference

@Service(Service.Level.PROJECT)
class MypyIncompleteConfigurationNotificationService(private val project: Project) {

    private var notification: WeakReference<Notification> = WeakReference(null)

    @Synchronized
    fun notify(canInstall: Boolean) {
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
        if (canInstall) {
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
        this.notification.get()?.expire()
        this.notification = WeakReference(notification)
        notification.notify(project)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypyIncompleteConfigurationNotificationService = project.service()
    }
}