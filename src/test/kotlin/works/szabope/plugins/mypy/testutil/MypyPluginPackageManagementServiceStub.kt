package works.szabope.plugins.mypy.testutil

import ai.grazie.utils.WeakHashMap
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.Version
import com.intellij.webcore.packaging.InstalledPackage
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.pyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService.Companion.MINIMUM_VERSION
import kotlin.Result.Companion.success

class MypyPluginPackageManagementServiceStub(
    override val project: Project
) : AbstractPluginPackageManagementService() {

    // support parallel runs
    private val installedPackagesPerSdk = WeakHashMap<Sdk, MutableList<InstalledPackage>>()

    override fun getInstalledVersion(): Version? {
        return getInstalledPackages().firstOrNull { it.name == getRequirement().name }?.version?.let {
            Version.parseVersion(
                it
            )
        }
    }

    override suspend fun installRequirement(): Result<Unit> {
        val r = getRequirement()
        getInstalledPackages().add(InstalledPackage(r.name, r.versionSpecs.firstOrNull()?.version))
        return success(Unit)
    }

    override suspend fun reloadPackages(): Result<List<PythonPackage>> {
        if (project.pythonSdk == null) {
            return success(emptyList())
        }
        return success(getInstalledPackages().map { PythonPackage(it.name, it.version!!, false) }.toList())
    }

    override fun getRequirement(): PyRequirement {
        return pyRequirement("mypy", PyRequirementRelation.GTE, MINIMUM_VERSION)
    }

    private fun getInstalledPackages(): MutableList<InstalledPackage> {
        var installedPackages = installedPackagesPerSdk[project.pythonSdk!!]
        if (installedPackages == null) {
            installedPackages = mutableListOf()
            installedPackagesPerSdk[project.pythonSdk!!] = installedPackages
        }
        return installedPackages
    }

}