@file:Suppress("removal", "UnstableApiUsage")

package works.szabope.plugins.mypy.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.pyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.PluginPackageManagementService

@Service(Service.Level.PROJECT)
class MypyPluginPackageManagementService(override val project: Project) : AbstractPluginPackageManagementService() {

    override fun getRequirement(): PyRequirement {
        return pyRequirement("mypy", PyRequirementRelation.GTE, MINIMUM_VERSION)
    }

    companion object {
        const val MINIMUM_VERSION = "1.11"

        @JvmStatic
        fun getInstance(project: Project): PluginPackageManagementService =
            project.service<MypyPluginPackageManagementService>()
    }
}
