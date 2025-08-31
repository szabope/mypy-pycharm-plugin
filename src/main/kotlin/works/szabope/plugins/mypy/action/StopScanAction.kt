package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.mypy.services.AsyncScanService

class StopScanAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        AsyncScanService.getInstance(event.project ?: return).cancelScan()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = AsyncScanService.getInstance(event.project ?: return).scanInProgress
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.StopScanAction"
    }
}
