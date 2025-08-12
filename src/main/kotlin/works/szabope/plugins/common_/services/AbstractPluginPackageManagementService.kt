package works.szabope.plugins.common_.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Version
import com.intellij.remote.RemoteSdkProperties
import com.jetbrains.python.errorProcessing.PyResult
import com.jetbrains.python.getOrThrow
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.common.PythonRepositoryPackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.management.findPackageSpecification
import com.jetbrains.python.packaging.management.toInstallRequest
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk

@Suppress("UnstableApiUsage")
abstract class AbstractPluginPackageManagementService : PluginPackageManagementService {

    protected abstract val project: Project

    override fun canInstall(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled()
    }

    override fun isLocalEnvironment(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    override suspend fun reloadPackages(): PyResult<List<PythonPackage>>? {
        return getPackageManager()?.reloadPackages()
    }

    override fun getInstalledVersion(): Version? {
        return getPackageManager()?.findPackageSpecification(getRequirement().name)?.versionSpec?.version?.let {
            Version.parseVersion(
                it
            )
        }
    }

    override fun isVersionSupported(version: Version): Boolean {
        return getRequirement().match(
            mutableListOf(
                PyPackage(
                    getRequirement().name, "${version.major}.${version.minor}.${version.bugfix}"
                )
            )
        ) != null
    }

    override fun isWSL() =
        (project.pythonSdk?.sdkAdditionalData as? RemoteSdkProperties)?.sdkId?.startsWith("WSL") ?: false

    fun getPackageManager(): PythonPackageManager? {
        return project.pythonSdk?.let { PythonPackageManager.forSdk(project, it) }
    }

    override fun isInstalled(): Boolean {
        return getInstalledVersion() != null
    }

    override suspend fun installRequirement(): Result<Unit> {
        val packageManager = getPackageManager()!!
        val requirement = getRequirement()
        val versionSpec = requirement.versionSpecs.firstOrNull()
        val specification = PythonRepositoryPackageSpecification(
            packageManager.repositoryManager.repositories.first(), requirement.name, versionSpec
        )
        try {
            packageManager.installPackage(specification.toInstallRequest(), options = emptyList()).getOrThrow()
        } catch (ex: PyExecutionException) {
            return Result.failure(ex)
        }
        return Result.success(Unit)
    }

    protected abstract fun getRequirement(): PyRequirement
}
