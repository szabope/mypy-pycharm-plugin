package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile

class RescanAction : ScanAction() {

    override fun listTargets(event: AnActionEvent): Collection<VirtualFile>? {
        val project = event.project ?: return null
        return getTreeService(project).getRootScanPaths()
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.RescanAction"
    }
}