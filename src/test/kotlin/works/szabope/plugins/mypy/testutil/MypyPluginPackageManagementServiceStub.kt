package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.pyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import works.szabope.plugins.common.test.services.AbstractPluginPackageManagementServiceStub
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService.Companion.MINIMUM_VERSION

class MypyPluginPackageManagementServiceStub(project: Project) : AbstractPluginPackageManagementServiceStub(project) {
    override fun getRequirement(): PyRequirement {
        return pyRequirement("mypy", PyRequirementRelation.GTE, MINIMUM_VERSION)
    }
}