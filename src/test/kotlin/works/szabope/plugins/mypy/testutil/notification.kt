package works.szabope.plugins.mypy.testutil

import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.test.notification.getConfigurationNotCompleteNotification
import works.szabope.plugins.mypy.MypyBundle

fun getMypyConfigurationNotCompleteNotification(project: Project): Notification =
    getConfigurationNotCompleteNotification(
        project,
        MypyBundle.message("notification.group.mypy.group"),
        MypyBundle.message("mypy.notification.incomplete_configuration")
    )
