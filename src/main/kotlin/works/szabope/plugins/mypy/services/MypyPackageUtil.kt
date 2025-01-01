package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.RepoPackage
import com.jetbrains.python.packaging.common.PackageManagerHolder
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk

object MypyPackageUtil {

    private const val PACKAGE_NAME = "mypy"

    fun canInstall(project: Project): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled(project)
    }

    fun getPackage() = RepoPackage(PACKAGE_NAME, null)

    @Suppress("IncorrectServiceRetrieving")
    fun getPackageManager(project: Project): PackageManagementService? {
        return project.pythonSdk?.let { sdk -> project.service<PackageManagerHolder>().bridgeForSdk(project, sdk) }
    }

    private fun isInstalled(project: Project): Boolean {
        return getPackageManager(project)?.installedPackagesList?.any { it.name == PACKAGE_NAME } ?: false
    }
}
