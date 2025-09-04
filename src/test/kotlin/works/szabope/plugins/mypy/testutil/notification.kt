package works.szabope.plugins.mypy.testutil

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import works.szabope.plugins.mypy.MypyBundle

context(project: Project) fun getPylintConfigurationNotCompleteNotification(): Notification {
    return ActionCenter.getNotifications(project).single {
        MypyBundle.message("mypy.notification.group") == it.groupId && MypyBundle.message("mypy.notification.incomplete_configuration") == it.content && !it.isExpired
    }
}