package works.szabope.plugins.mypy.activity

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import works.szabope.plugins.mypy.services.IncompleteConfigurationNotificationService
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings
import works.szabope.plugins.mypy.services.SettingsValidator

internal class SettingsInitializationActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        configurePlugin(project)
    }

    private suspend fun configurePlugin(project: Project) {
        if (!ApplicationManager.getApplication().isUnitTestMode) {
            MypyPluginPackageManagementService.getInstance(project).reloadPackages()
        }
        val settings = MypySettings.getInstance(project)
        settings.initSettings(OldMypySettings.getInstance(project))
        if (!SettingsValidator(project).isComplete(settings.getData())) {
            val notificationService = IncompleteConfigurationNotificationService.getInstance(project)
            val canInstall = MypyPluginPackageManagementService.getInstance(project).canInstall()
            notificationService.notify(canInstall)
        }
    }
}
