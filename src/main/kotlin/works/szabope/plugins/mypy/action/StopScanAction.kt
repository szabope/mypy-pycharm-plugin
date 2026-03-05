package works.szabope.plugins.mypy.action

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.action.AbstractScanJobRegistry
import works.szabope.plugins.common.action.AbstractStopScanAction
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

class StopScanAction : AbstractStopScanAction() {

    override fun getScanJobRegistry(project: Project): AbstractScanJobRegistry = MypyScanJobRegistryService.getInstance(project)
    override fun getTreeService(project: Project): ITreeService = MypyTreeService.getInstance(project)

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.StopScanAction"
    }
}
