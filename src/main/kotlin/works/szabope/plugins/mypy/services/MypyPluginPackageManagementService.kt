@file:Suppress("removal", "UnstableApiUsage")

package works.szabope.plugins.mypy.services

import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.pyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import works.szabope.plugins.common_.services.AbstractPluginPackageManagementService

class MypyPluginPackageManagementService(override val project: Project) : AbstractPluginPackageManagementService() {

    companion object {
        const val MINIMUM_VERSION = "1.11"
    }

    override fun getRequirement(): PyRequirement {
        return pyRequirement("mypy", PyRequirementRelation.GTE, MINIMUM_VERSION)
    }
}
