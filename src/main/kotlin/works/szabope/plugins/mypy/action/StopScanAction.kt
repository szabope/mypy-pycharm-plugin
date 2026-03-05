package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.future.future
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

class StopScanAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        currentThreadCoroutineScope().future {
            event.project?.let { MypyScanJobRegistryService.getInstance(it).cancel() }
            event.project?.let { MypyTreeService.getInstance(it) }?.lock()
        }.get()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { MypyScanJobRegistryService.getInstance(it).isActive() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.StopScanAction"
    }
}
