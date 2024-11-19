package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.toRunConfiguration

class RescanWithMypyAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val latestScanTargets = getPanel(project).getScanTargets()
        val runConfiguration = project.let { MypySettings.getInstance(it).toRunConfiguration() }
        getPanel(project).initializeResultTree(latestScanTargets)
        MypyService.getInstance(project).scanAsync(latestScanTargets, runConfiguration)
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        event.presentation.isEnabled = getPanel(project).getScanTargets().isNotEmpty() && isReadyToScan(project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}