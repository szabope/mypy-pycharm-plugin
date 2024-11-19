package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.mypy.services.MypyService

class StopCheckAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        MypyService.getInstance(event.project ?: return).cancelScan()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = MypyService.getInstance(event.project ?: return).scanInProgress
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}