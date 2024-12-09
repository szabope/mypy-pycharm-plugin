package works.szabope.plugins.mypy.services

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.RepoPackage
import com.jetbrains.python.packaging.bridge.PythonPackageManagementServiceBridge
import com.jetbrains.python.packaging.common.PackageManagerHolder
import com.jetbrains.python.sdk.pythonSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Service(Service.Level.PROJECT)
class MypyPackageManagerService(private val project: Project, private val cs: CoroutineScope) {

    fun install(
        handleSuccess: suspend () -> Unit,
        handleFailure: suspend (description: PackageManagementService.ErrorDescription) -> Unit
    ) {
        val sdk = requireNotNull(ProjectRootManager.getInstance(project).projectSdk)
        val packageManager = getPackageManager(sdk)
        val mypyPackage = RepoPackage(PACKAGE_NAME, null)
        val listener = object : PackageManagementService.Listener {
            override fun operationStarted(packageName: String?) = Unit

            override fun operationFinished(
                ignored: String?, errorDesc: PackageManagementService.ErrorDescription?
            ) {
                when (errorDesc) {
                    null -> cs.launch { handleSuccess() }
                    else -> cs.launch { handleFailure(errorDesc) }
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
        val sdk = project.modules.firstNotNullOfOrNull { it.pythonSdk } ?: return false
        return sdk.sdkType.isLocalSdk(sdk) && !isInstalled(sdk)
    }

    private fun isInstalled(sdk: Sdk): Boolean {
        return getPackageManager(sdk).installedPackagesList.any { it.name == PACKAGE_NAME }
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
