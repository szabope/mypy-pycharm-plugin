package works.szabope.plugins.mypy.action

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.action.AbstractScanJobRegistry

@Service(Service.Level.PROJECT)
class MypyScanJobRegistryService : AbstractScanJobRegistry() {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): MypyScanJobRegistryService = project.service()
    }
}