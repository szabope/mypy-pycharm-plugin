package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.services.IncompleteConfigurationNotifier
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.action.InstallMypyAction
import works.szabope.plugins.mypy.action.OpenSettingsAction

@Service(Service.Level.PROJECT)
class MypyIncompleteConfigurationNotifier(project: Project) : IncompleteConfigurationNotifier(
    project,
    MypyBundle.message("notification.group.mypy.group"),
    MypyBundle.message("mypy.notification.incomplete_configuration"),
    OpenSettingsAction.ID,
    InstallMypyAction.ID,
) {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypyIncompleteConfigurationNotifier =
            project.service<MypyIncompleteConfigurationNotifier>()
    }
}
