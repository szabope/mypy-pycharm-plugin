package works.szabope.plugins.mypy.activity

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import works.szabope.plugins.mypy.services.IncompleteConfigurationNotifier
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

internal class SettingsInitializationActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isDefault) {
            return
        }
        if (!ApplicationManager.getApplication().isUnitTestMode) {
            MypyPluginPackageManagementService.getInstance(project).reloadPackages()
        }
        val settings = MypySettings.getInstance(project)
        // we trust in old settings validity
        settings.initSettings(OldMypySettings.getInstance(project))
        if (settings.getValidConfiguration().isFailure) {
            val canInstall = MypyPluginPackageManagementService.getInstance(project).canInstall()
            IncompleteConfigurationNotifier.notify(project, canInstall)
        }
    }
}
