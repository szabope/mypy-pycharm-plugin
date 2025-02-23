@file:Suppress("removal", "UnstableApiUsage")

package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.webcore.packaging.PackageManagementService
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.packaging.PyPackageVersionComparator
import com.jetbrains.python.packaging.common.PythonSimplePackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import com.jetbrains.python.packaging.ui.PyPackageManagementService
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk

object MypyPackageUtil {

    const val MINIMUM_VERSION = "1.11"

    private val PACKAGE = PythonSimplePackageSpecification(
        "mypy", MINIMUM_VERSION, null, PyRequirementRelation.GTE
    )

    fun canInstall(project: Project): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled(project)
    }

    suspend fun reloadPackages(project: Project) = try {
        getPackageManager(project)?.reloadPackages()
    } catch (e: Exception) {
        // e.g. org.apache.hc.client5.http.HttpHostConnectException thrown when docker (in given SDK) is unavailable
        Result.failure(e)
    }

    fun isVersionSupported(version: String): Boolean {
        return PyPackageVersionComparator.STR_COMPARATOR.compare(version, MINIMUM_VERSION) >= 0
    }

    private fun getPackageManager(project: Project): PythonPackageManager? {
        return getSdk(project)?.let { PythonPackageManager.forSdk(project, it) }
    }

    private fun getSdk(project: Project): Sdk? {
        return project.pythonSdk
    }

    private fun isInstalled(project: Project): Boolean {
        return getPackageManager(project)?.installedPackages?.any { it.name == PACKAGE.name } ?: false
    }

    suspend fun install(project: Project): PackageManagementService.ErrorDescription? {
        if (isInstalled(project)) return null
        val packageManager = getPackageManager(project)!!
        try {
            packageManager.installPackage(PACKAGE, emptyList()).getOrThrow()
        } catch (ex: PyExecutionException) {
            return PyPackageManagementService.toErrorDescription(listOf(ex), getSdk(project), PACKAGE.name)
        }
        return null
    }
}
