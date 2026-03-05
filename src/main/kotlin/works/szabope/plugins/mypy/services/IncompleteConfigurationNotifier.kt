package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.notifyIncompleteConfiguration
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.action.InstallMypyAction
import works.szabope.plugins.mypy.action.OpenSettingsAction

class IncompleteConfigurationNotifier {
    companion object {
        @JvmStatic
        fun notify(project: Project, canInstall: Boolean) {
            notifyIncompleteConfiguration(
                project,
                MypyBundle.message("notification.group.mypy.group"),
                MypyBundle.message("mypy.notification.incomplete_configuration"),
                OpenSettingsAction.ID,
                InstallMypyAction.ID,
                canInstall
            )
        }
    }
}
