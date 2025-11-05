package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteIntentReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.mypy.services.AsyncScanService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

class RescanAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val treeService = MypyTreeService.getInstance(project)
        val latestScanTargets = treeService.getRootScanPaths()
        treeService.reinitialize(latestScanTargets)
        WriteIntentReadAction.run { FileDocumentManager.getInstance().saveAllDocuments() }
        AsyncScanService.getInstance(project).scan(latestScanTargets, MypySettings.getInstance(project).getData())
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val treeService = MypyTreeService.getInstance(project)
        event.presentation.isEnabled =
            treeService.getRootScanPaths().isNotEmpty() && ScanActionUtil.isReadyToScan(project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }


    companion object {
        const val ID = "works.szabope.plugins.mypy.action.RescanAction"
    }
}