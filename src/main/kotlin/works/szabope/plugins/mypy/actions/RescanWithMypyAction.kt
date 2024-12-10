package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.toRunConfiguration
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

class RescanWithMypyAction : AbstractScanAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val panel = event.getData(MypyToolWindowPanel.MYPY_PANEL_DATA_KEY) ?: return
        val latestScanTargets = panel.getScanTargets()
        val runConfiguration = project.let { MypySettings.getInstance(it).toRunConfiguration() }
        panel.initializeResultTree(latestScanTargets)
        MypyService.getInstance(project).scanAsync(latestScanTargets, runConfiguration)
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val panel = event.getData(MypyToolWindowPanel.MYPY_PANEL_DATA_KEY) ?: return
        event.presentation.isEnabled = panel.getScanTargets().isNotEmpty() && isReadyToScan(project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}