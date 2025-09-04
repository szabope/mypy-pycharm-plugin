package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.pyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import works.szabope.plugins.common.test.services.AbstractPluginPackageManagementServiceStub

class MypyPluginPackageManagementServiceStub(project: Project) : AbstractPluginPackageManagementServiceStub(project) {
    override fun getRequirement(): PyRequirement {
        return pyRequirement("mypy", PyRequirementRelation.GTE, "1.11")
    }
}
