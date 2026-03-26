package works.szabope.plugins.mypy.configurable

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.validator.AbstractToolValidator
import works.szabope.plugins.common.validator.ToolValidatorMessages
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService

class MypyValidator(project: Project) : AbstractToolValidator(project, MESSAGES) {
    override val versionFlag = "-V"
    override val packageName = "mypy"
    override fun getPackageManagementService() = MypyPluginPackageManagementService.getInstance(project)

    companion object {
        private val MESSAGES = ToolValidatorMessages(
            pathNotExists = MypyBundle.message("mypy.configuration.path_to_executable.not_exists"),
            pathIsDirectory = MypyBundle.message("mypy.configuration.path_to_executable.is_directory"),
            pathNotExecutable = MypyBundle.message("mypy.configuration.path_to_executable.not_executable"),
            unknownVersion = MypyBundle.message("mypy.configuration.path_to_executable.unknown_version"),
            invalidVersion = MypyBundle.message("mypy.configuration.mypy_invalid_version"),
            notInstalled = MypyBundle.message("mypy.configuration.mypy_not_installed")
        )
    }
}