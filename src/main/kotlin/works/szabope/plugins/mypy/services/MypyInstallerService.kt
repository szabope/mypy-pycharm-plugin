@file:Suppress("removal", "DEPRECATION")

package works.szabope.plugins.mypy.services

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.use
import com.intellij.util.io.await
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.RepoPackage
import com.jetbrains.python.packaging.PyPackageManagers
import com.jetbrains.python.packaging.common.PackageManagerHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
class MypyInstallerService(private val project: Project, val cs: CoroutineScope) {

    suspend fun installMypy() {
        val sdk = requireNotNull(ProjectRootManager.getInstance(project).projectSdk)
        withContext(Dispatchers.EDT) {
            installMypy(sdk)
        }
    }

    fun isMypyInstalled(): Boolean {
        val sdk = requireNotNull(ProjectRootManager.getInstance(project).projectSdk)
        return runBlocking(Dispatchers.IO) {
            PackageManagerHolder().use { manager ->
                manager.bridgeForSdk(project, sdk).installedPackagesList.find { it.name == PACKAGE_NAME } != null
            }
        }
    }

    // PackageManagerHolder.bridgeForSdk.installPackage does not call `operationFinished` in listener
    private suspend fun installMypy(sdk: Sdk): PackageManagementService.ErrorDescription? {
        val manager = PyPackageManagers.getInstance().getManagementService(project, sdk)
        val mypyPackage = RepoPackage(PACKAGE_NAME, null)
        val result = CompletableFuture<PackageManagementService.ErrorDescription>()
        val listener = object : PackageManagementService.Listener {
            override fun operationStarted(packageName: String?) = Unit
            override fun operationFinished(ignored: String?, errorDesc: PackageManagementService.ErrorDescription?) {
                result.complete(errorDesc)
            }
        }
        manager.installPackage(mypyPackage, null, false, null, listener, false)
        return result.await()
    }

    companion object {
        const val PACKAGE_NAME = "mypy"

        @JvmStatic
        fun getInstance(project: Project): MypyInstallerService = project.service()
    }
}
