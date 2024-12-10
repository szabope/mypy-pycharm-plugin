package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.webcore.packaging.RepoPackage
import com.jetbrains.python.packaging.bridge.PythonPackageManagementServiceBridge
import com.jetbrains.python.packaging.common.PackageManagerHolder
import com.jetbrains.python.sdk.pythonSdk

object MypyPackageUtil {

    private const val PACKAGE_NAME = "mypy"

    fun canInstall(project: Project): Boolean {
        return isLocalSdk(project) && !isInstalled(project)
    }

    fun getPackage() = RepoPackage(PACKAGE_NAME, null)

    @Suppress("IncorrectServiceRetrieving")
    fun getPackageManager(project: Project): PythonPackageManagementServiceBridge? {
        return getSdk(project)?.let { sdk -> project.service<PackageManagerHolder>().bridgeForSdk(project, sdk) }
    }

    private fun getSdk(project: Project) = project.modules.firstNotNullOfOrNull { it.pythonSdk }

    private fun isLocalSdk(project: Project) = getSdk(project)?.let { it.sdkType.isLocalSdk(it) } == true

    private fun isInstalled(project: Project): Boolean {
        return getPackageManager(project)?.installedPackagesList?.any { it.name == PACKAGE_NAME } ?: false
    }
}
