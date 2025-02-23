package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.webcore.packaging.InstalledPackage
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.common.PythonPackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.management.PythonRepositoryManager

@Suppress("UnstableApiUsage")
class TestPythonPackageManager(project: Project, sdk: Sdk) :
    PythonPackageManager(project, sdk) {
    private val testInstalledPackages = mutableListOf<InstalledPackage>()
    override val installedPackages: List<PythonPackage>
        get() = testInstalledPackages.map { PythonPackage(it.name, it.version ?: "3.3.4", false) }.toList()

    override val repositoryManager: PythonRepositoryManager
        get() = throw NotImplementedError()

    override suspend fun installPackage(
        specification: PythonPackageSpecification, options: List<String>
    ): Result<List<PythonPackage>> {
        testInstalledPackages.add(InstalledPackage(specification.name, "1.11.0"))
        return reloadPackages()
    }

    override suspend fun reloadPackages(): Result<List<PythonPackage>> {
        return Result.success(testInstalledPackages.map { PythonPackage(it.name, it.version ?: "1.11.0", false) }
            .toList())
    }

    override suspend fun uninstallPackage(pkg: PythonPackage): Result<List<PythonPackage>> {
        throw NotImplementedError()
    }

    override suspend fun updatePackage(specification: PythonPackageSpecification): Result<List<PythonPackage>> {
        throw NotImplementedError()
    }
}