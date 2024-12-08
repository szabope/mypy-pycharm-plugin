package works.szabope.plugins.mypy.services

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.RepoPackage
import com.jetbrains.python.packaging.bridge.PythonPackageManagementServiceBridge
import com.jetbrains.python.packaging.common.PackageManagerHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Service(Service.Level.PROJECT)
class MypyPackageManagerService(private val project: Project, private val cs: CoroutineScope) {

    fun install(handleSuccess: suspend (project: Project) -> Unit, handleFailure: suspend (project: Project) -> Unit) {
        val sdk = requireNotNull(ProjectRootManager.getInstance(project).projectSdk)
        val packageManager = getPackageManager(sdk)
        val mypyPackage = RepoPackage(PACKAGE_NAME, null)
        val listener = object : PackageManagementService.Listener {
            override fun operationStarted(packageName: String?) = Unit

            override fun operationFinished(
                ignored: String?, errorDesc: PackageManagementService.ErrorDescription?
            ) {
                when (errorDesc) {
                    null -> cs.launch { handleSuccess(project) }
                    else -> cs.launch { handleFailure(project) }
                }
            }
        }
        cs.launch {
            withContext(Dispatchers.EDT) {
                packageManager.installPackage(mypyPackage, null, false, null, listener, false)
            }
        }
    }

    fun canInstall(): Boolean {
        val sdk = ProjectRootManager.getInstance(project).projectSdk ?: return false
        return sdk.sdkType.isLocalSdk(sdk) && !isInstalled(sdk)
    }

    private fun isInstalled(sdk: Sdk): Boolean {
        return getPackageManager(sdk).installedPackagesList.find { it.name == PACKAGE_NAME } != null
    }

    @Suppress("IncorrectServiceRetrieving")
    private fun getPackageManager(sdk: Sdk): PythonPackageManagementServiceBridge {
        return project.service<PackageManagerHolder>().bridgeForSdk(project, sdk)
    }

    companion object {
        private const val PACKAGE_NAME = "mypy"

        @JvmStatic
        fun getInstance(project: Project): MypyPackageManagerService = project.service()
    }
}
