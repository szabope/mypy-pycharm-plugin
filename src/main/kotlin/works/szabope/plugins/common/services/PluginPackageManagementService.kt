@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.common.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Version
import com.jetbrains.python.errorProcessing.PyResult
import com.jetbrains.python.packaging.common.PythonPackage

interface PluginPackageManagementService {
    fun canInstall(): Boolean
    fun isLocalEnvironment(): Boolean

    suspend fun reloadPackages(): PyResult<List<PythonPackage>>?
    fun getInstalledVersion(): Version?
    fun isVersionSupported(version: Version): Boolean
    fun isInstalled(): Boolean
    fun isWSL(): Boolean

    suspend fun installRequirement(): Result<Unit>

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PluginPackageManagementService = project.service()
    }
}