package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.RepoPackage
import com.jetbrains.python.packaging.common.PackageManagerHolder
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

object MypyPackageUtil {

    private val PACKAGE = RepoPackage("mypy", null)

    fun canInstall(project: Project): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled(project)
    }

    @Suppress("IncorrectServiceRetrieving")
    fun getPackageManager(project: Project): PackageManagementService? {
        return project.pythonSdk?.let { sdk -> project.service<PackageManagerHolder>().bridgeForSdk(project, sdk) }
    }

    private fun isInstalled(project: Project): Boolean {
        return getPackageManager(project)?.installedPackagesList?.any { it.name == PACKAGE.name } ?: false
    }

    suspend fun install(project: Project): PackageManagementService.ErrorDescription? {
        val packageManager = getPackageManager(project)!!
        val result = CompletableFuture<PackageManagementService.ErrorDescription>()
        val listener = object : PackageManagementService.Listener {
            override fun operationStarted(packageName: String?) = Unit
            override fun operationFinished(ignored: String?, error: PackageManagementService.ErrorDescription?) {
                if (error == null) {
                    result.complete(null)
                } else {
                    result.complete(error)
                }
            }
        }
        packageManager.installPackage(PACKAGE, null, false, null, listener, false)
        return result.await()
    }

}
