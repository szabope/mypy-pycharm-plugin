package works.szabope.plugins.mypy.configurable

import com.intellij.collaboration.util.ResultUtil.processErrorAndGet
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.ui.layout.ValidationInfoBuilder
import com.jetbrains.python.packaging.PyPackage
import kotlinx.coroutines.flow.first
import works.szabope.plugins.common.run.CliExecutionEnvironmentFactory
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
import works.szabope.plugins.common.services.PluginPackageManagementService.PluginPackageManagementException.PackageNotInstalledException
import works.szabope.plugins.common.services.PluginPackageManagementService.PluginPackageManagementException.PackageVersionObsoleteException
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import java.io.File

class MypyValidator(private val project: Project) {
    fun validateExecutable(path: String?, builder: ValidationInfoBuilder): ValidationInfo? {
        val path = path ?: return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return builder.error(MypyBundle.message("mypy.configuration.path_to_executable.not_exists"))
        }
        if (file.isDirectory) {
            return builder.error(MypyBundle.message("mypy.configuration.path_to_executable.is_directory"))
        }
        if (!file.canExecute()) {
            return builder.error(MypyBundle.message("mypy.configuration.path_to_executable.not_executable"))
        }
        val environment = CliExecutionEnvironmentFactory(project).createEnvironment(path, listOf("-V"))
        val mypyVersion = runWithModalProgressBlocking(
            project, MypyBundle.message("mypy.configuration.path_to_executable.version_validation_title")
        ) {
            execute(environment).runCatching {
                first().let { "(\\d+.\\d+.\\d+)".toRegex().find(it)?.groups?.last()?.value }
            }
        }.processErrorAndGet {
            if (it is ProcessException) {
                return builder.error(
                    MypyBundle.message(
                        "mypy.configuration.path_to_executable.exited_with_error", path, it.exitCode, it.stdErr
                    )
                )
            }
            thisLogger().error("Error while executing mypy", it)
        }
        if (mypyVersion == null) {
            return builder.error(MypyBundle.message("mypy.configuration.path_to_executable.unknown_version"))
        }
        if (!MypyPluginPackageManagementService.getInstance(project).getRequirement()
                .match(PyPackage("mypy", mypyVersion))
        ) {
            return builder.error(MypyBundle.message("mypy.configuration.mypy_invalid_version"))
        }
        return null
    }

    fun validateSdk(builder: ValidationInfoBuilder): ValidationInfo? {
        if (MypyPluginPackageManagementService.getInstance(project).isWSL()) {
            return builder.error(MypyBundle.message("mypy.configuration.wsl_not_supported"))
        }
        MypyPluginPackageManagementService.getInstance(project).checkInstalledRequirement().onFailure {
            when (it) {
                is PackageNotInstalledException -> return builder.error(MypyBundle.message("mypy.configuration.mypy_not_installed"))
                is PackageVersionObsoleteException -> return builder.error(MypyBundle.message("mypy.configuration.mypy_invalid_version"))
            }
        }
        return null
    }
}
