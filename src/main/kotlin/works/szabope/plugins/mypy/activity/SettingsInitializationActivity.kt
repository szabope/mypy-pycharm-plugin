package works.szabope.plugins.mypy.activity

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.activity.AbstractSettingsInitializationActivity
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.mypy.services.MypyIncompleteConfigurationNotifier
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings

class SettingsInitializationActivity : AbstractSettingsInitializationActivity() {

    override fun getPackageManagementService(project: Project): AbstractPluginPackageManagementService =
        MypyPluginPackageManagementService.getInstance(project)

    override fun getSettings(project: Project): Settings = MypySettings.getInstance(project)

    override suspend fun getOldSettings(project: Project): BasicSettingsData = OldMypySettings.getInstance(project)

    override fun notifyIncomplete(project: Project, canInstall: Boolean) =
        MypyIncompleteConfigurationNotifier.getInstance(project).showWarningBubble(canInstall)
}
