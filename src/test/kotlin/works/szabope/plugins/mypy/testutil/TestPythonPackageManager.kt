package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.webcore.packaging.InstalledPackage
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.common.PythonPackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.management.PythonRepositoryManager
import kotlin.Result.Companion.success

@Suppress("UnstableApiUsage")
class TestPythonPackageManager(project: Project, sdk: Sdk) :
    PythonPackageManager(project, sdk) {
    private val testInstalledPackages = mutableListOf<InstalledPackage>()
    override var installedPackages: List<PythonPackage> = emptyList()
        get() = testInstalledPackages.map { PythonPackage(it.name, it.version ?: "3.3.4", false) }.toList()

    override val repositoryManager: PythonRepositoryManager
        get() = throw NotImplementedError()

    override suspend fun installPackageCommand(
        specification: PythonPackageSpecification,
        options: List<String>
    ): Result<Unit> {
        testInstalledPackages.add(InstalledPackage(specification.name, "1.11.0"))
        return reloadPackages().map { }
    }

    override suspend fun reloadPackages(): Result<List<PythonPackage>> {
        return success(testInstalledPackages.map { PythonPackage(it.name, it.version!!, false) }.toList())
    }

    override suspend fun uninstallPackageCommand(pkg: PythonPackage): Result<Unit> {
        throw NotImplementedError()
    }

    override suspend fun reloadPackagesCommand(): Result<List<PythonPackage>> {
        return success(installedPackages)
    }

    override suspend fun updatePackageCommand(specification: PythonPackageSpecification): Result<Unit> {
        return success(Unit)
    }
}