package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.services.ImmutableSettingsData

class SettingsValidator(private val project: Project) {

    fun isComplete(configuration: ImmutableSettingsData): Boolean {
        return if (configuration.useProjectSdk) {
            project.pythonSdk != null && MypyPluginPackageManagementService.getInstance(project)
                .checkInstalledRequirement().isSuccess
        } else {
            configuration.executablePath.isNotBlank()
        }
    }
}