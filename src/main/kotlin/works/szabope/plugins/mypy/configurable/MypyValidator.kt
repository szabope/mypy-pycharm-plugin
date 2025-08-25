package works.szabope.plugins.mypy.configurable

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Version
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.ui.layout.ValidationInfoBuilder
import works.szabope.plugins.common.run.CliExecutionEnvironmentFactory
import works.szabope.plugins.common.run.ProcessException
import works.szabope.plugins.common.run.execute
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
        val mypyVersion = runWithModalProgressBlocking(
            project, MypyBundle.message("mypy.configuration.path_to_executable.version_validation_title")
        ) {
            val environment = CliExecutionEnvironmentFactory(project).createEnvironment("$path -V")
            execute(environment).mapCatching { it ->
                val stdout = buildString { it.collect { appendLine(it) } }
                return@mapCatching "(\\d+.\\d+.\\d+)".toRegex().find(stdout)?.groups?.last()?.value
            }
        }.onFailure {
            if (it is ProcessException) {
                return builder.error(
                    MypyBundle.message(
                        "mypy.configuration.path_to_executable.exited_with_error", path, it.exitCode, it.stdErr
                    )
                )
            }
            thisLogger().error("Error while executing mypy", it)
            return null
        }.getOrNull()
        if (mypyVersion == null) {
            return builder.error(MypyBundle.message("mypy.configuration.path_to_executable.unknown_version"))
        }
        return validateVersion(builder, Version.parseVersion(mypyVersion)!!)
    }

    fun validateSdk(builder: ValidationInfoBuilder): ValidationInfo? {
        if (MypyPluginPackageManagementService.getInstance(project).isWSL()) {
            return builder.error(MypyBundle.message("mypy.configuration.wsl_not_supported"))
        }
        val installedPackage =
            MypyPluginPackageManagementService.getInstance(project).getInstalledVersion() ?: return builder.error(
                MypyBundle.message("mypy.configuration.mypy_not_installed")
            )
        return validateVersion(builder, installedPackage)
    }

    private fun validateVersion(builder: ValidationInfoBuilder, version: Version): ValidationInfo? {
        if (!MypyPluginPackageManagementService.getInstance(project).isVersionSupported(version)) {
            return builder.error(
                MypyBundle.message(
                    "mypy.configuration.mypy_invalid_version", "${version.major}.${version.minor}.${version.bugfix}"
                )
            )
        }
        return null
    }
}
