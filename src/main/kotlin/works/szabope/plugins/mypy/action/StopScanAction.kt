package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.guava.future

class StopScanAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        currentThreadCoroutineScope().future {
            ScanJobRegistry.INSTANCE.cancel()
        }.get()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = ScanJobRegistry.INSTANCE.isActive()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.StopScanAction"
    }
}
