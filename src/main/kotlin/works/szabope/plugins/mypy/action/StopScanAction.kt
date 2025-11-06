package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class StopScanAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        ScanJobRegistry.INSTANCE.cancel()
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
