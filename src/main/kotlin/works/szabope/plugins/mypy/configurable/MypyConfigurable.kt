package works.szabope.plugins.mypy.configurable

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import works.szabope.plugins.common.configurable.ConfigurableConfiguration
import works.szabope.plugins.common.configurable.GeneralConfigurable
import works.szabope.plugins.common.trimToNull
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.action.InstallMypyAction
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.services.MypySettings

class MypyConfigurable(private val project: Project) : GeneralConfigurable(
    project, ConfigurableConfiguration(
        MypyBundle.message("mypy.configuration.name"),
        MypyBundle.message("mypy.configuration.name"),
        ID,
        InstallMypyAction.ID,
        MypyBundle.message("mypy.intention.install_mypy.text"),
        MypyBundle.message("mypy.configuration.mypy_picker_title"),
        MypyBundle.message("mypy.configuration.path_to_executable.label"),
        FileFilter(
            if (SystemInfo.isWindows) {
                listOf("mypy.exe", "mypyc.exe", "mypy.bat")
            } else {
                listOf("mypy", "mypyc")
            }
        ),
        MypyBundle.message("mypy.configuration.path_to_executable.empty_warning"),
        MypyBundle.message("mypy.configuration.path_to_executable.version_validation_title"),
        MypyBundle.message("mypy.configuration.use_project_sdk"),
        MypyBundle.message("mypy.configuration.config_file.comment"),
        MypyBundle.message("mypy.configuration.config_file.help")
    )
) {
    override val settings get() = MypySettings.getInstance(project)
    override val packageManager get() = MypyPluginPackageManagementService.getInstance(project)

    override fun validateExecutable(path: String?) = with(MypyValidator(project)) {
        path?.trimToNull()?.let { path ->
            validateExecutablePath(path) ?: validateMypyVersion(path)
        }
    }

    override fun validateLocalSdk() = MypyValidator(project).validateProjectSdk()

    override fun validateConfigFilePath(
        builder: ValidationInfoBuilder, field: TextFieldWithBrowseButton
    ) = MypyConfigFileValidator().validateConfigFilePath(field.text.trimToNull())?.let { builder.error(it) }

    companion object {
        const val ID = "Settings.Mypy"
    }
}