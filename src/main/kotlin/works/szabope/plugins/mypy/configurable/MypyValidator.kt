package works.szabope.plugins.mypy.configurable

import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.target.TargetedCommandLineBuilder
import com.intellij.execution.target.local.LocalTargetEnvironment
import com.intellij.execution.target.local.LocalTargetEnvironmentRequest
import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyPackage
import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import java.io.File

class MypyValidator(private val project: Project) {
    fun validateExecutablePath(path: String?): String? {
        val path = path ?: return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return MypyBundle.message("mypy.configuration.path_to_executable.not_exists")
        }
        if (file.isDirectory) {
            return MypyBundle.message("mypy.configuration.path_to_executable.is_directory")
        }
        if (!file.canExecute()) {
            return MypyBundle.message("mypy.configuration.path_to_executable.not_executable")
        }
        return null
    }

    fun validateMypyVersion(path: String): String? {
        val mypyVersion = getVersionForExecutable(path)
            ?: return MypyBundle.message("mypy.configuration.path_to_executable.unknown_version")
        if (!MypyPluginPackageManagementService.getInstance(project).getRequirement()
                .match(PyPackage("mypy", mypyVersion))
        ) {
            return MypyBundle.message("mypy.configuration.mypy_invalid_version")
        }

        return null
    }

    private fun getVersionForExecutable(pathToExecutable: String): String? {
        val targetEnvRequest = LocalTargetEnvironmentRequest()
        val targetEnvironment = LocalTargetEnvironment(LocalTargetEnvironmentRequest())

        val commandLineBuilder = TargetedCommandLineBuilder(targetEnvRequest)
        commandLineBuilder.setExePath(pathToExecutable)
        commandLineBuilder.addParameters("-V")

        val targetCMD = commandLineBuilder.build()

        val process = targetEnvironment.createProcess(targetCMD)

        return runCatching {
            val processHandler = CapturingProcessHandler(
                process, targetCMD.charset, targetCMD.getCommandPresentation(targetEnvironment)
            )
            val processOutput = processHandler.runProcess(5000, true).stdout
            "(\\d+.\\d+.\\d+)".toRegex().find(processOutput)?.groups?.last()?.value
        }.getOrNull()
    }

    fun validateSdk(): String? {
        if (MypyPluginPackageManagementService.getInstance(project).isWSL()) {
            return MypyBundle.message("mypy.configuration.wsl_not_supported")
        }
        MypyPluginPackageManagementService.getInstance(project).checkInstalledRequirement().onFailure {
            when (it) {
                is PluginPackageManagementException.PackageNotInstalledException -> return MypyBundle.message(
                    "mypy.configuration.mypy_not_installed"
                )

                is PluginPackageManagementException.PackageVersionObsoleteException -> return MypyBundle.message(
                    "mypy.configuration.mypy_invalid_version"
                )
            }
        }
        return null
    }
}