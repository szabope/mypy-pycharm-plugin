package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

class RescanAction : ScanAction() {

    override fun listTargets(event: AnActionEvent): Collection<VirtualFile> {
        return MypyTreeService.getInstance(event.project ?: return emptyList()).getRootScanPaths()
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.RescanAction"
    }
}