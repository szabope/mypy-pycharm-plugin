package works.szabope.plugins.mypy.configurable

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.layout.ValidationInfoBuilder
import works.szabope.plugins.common.configurable.ConfigurableConfiguration
import works.szabope.plugins.common.configurable.GeneralConfigurable
import works.szabope.plugins.common.trimToNull
import works.szabope.plugins.mypy.MypyArgs
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.actions.InstallMypyAction
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
        MypyBundle.message("mypy.configuration.use_project_sdk"),
        MypyBundle.message("mypy.configuration.config_file.comment"),
        MypyArgs.MYPY_RECOMMENDED_COMMAND_ARGS
    )
) {
    override val settings get() = MypySettings.getInstance(project)
    override val packageManager get() = MypyPluginPackageManagementService.getInstance(project)
    override val defaultArguments = MypyArgs.MYPY_RECOMMENDED_COMMAND_ARGS

    override fun validateExecutable(
        builder: ValidationInfoBuilder,
        field: TextFieldWithBrowseButton
    ) = MypyValidator(project).validateExecutable(field.text.trimToNull(), builder)

    override fun validateSdk(
        builder: ValidationInfoBuilder,
        button: JBRadioButton
    ) = MypyValidator(project).validateSdk(builder)

    override fun validateConfigFilePath(
        builder: ValidationInfoBuilder,
        field: TextFieldWithBrowseButton
    ) = MypyConfigFileValidator().validateConfigFilePath(field.text.trimToNull(), builder)

    companion object {
        const val ID = "Settings.Mypy"
    }
}