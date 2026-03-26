package works.szabope.plugins.mypy.configurable

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.validator.AbstractToolValidator
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService

class MypyValidator(project: Project) : AbstractToolValidator(project) {
    override val versionFlag = "-V"
    override val packageName = "mypy"
    override fun getPackageManagementService() = MypyPluginPackageManagementService.getInstance(project)
    override fun pathNotExistsMessage() = MypyBundle.message("mypy.configuration.path_to_executable.not_exists")
    override fun pathIsDirectoryMessage() = MypyBundle.message("mypy.configuration.path_to_executable.is_directory")
    override fun pathNotExecutableMessage() = MypyBundle.message("mypy.configuration.path_to_executable.not_executable")
    override fun unknownVersionMessage() = MypyBundle.message("mypy.configuration.path_to_executable.unknown_version")
    override fun invalidVersionMessage() = MypyBundle.message("mypy.configuration.mypy_invalid_version")
    override fun notInstalledMessage() = MypyBundle.message("mypy.configuration.mypy_not_installed")
}