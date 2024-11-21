package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.mypy.services.MypyService
import works.szabope.plugins.mypy.services.MypyService.Companion.SUPPORTED_FILE_TYPES
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.toRunConfiguration
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

open class ScanWithMypyAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val targets = listTargets(event)?.map { it.path } ?: return
        val project = event.project ?: return
        val runConfiguration = MypySettings.getInstance(project).toRunConfiguration()
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(MypyToolWindowPanel.ID) ?: return
        (toolWindow.contentManager.getContent(0)?.component as MypyToolWindowPanel?)?.initializeResultTree(targets)
        MypyService.getInstance(project).scanAsync(targets, runConfiguration)
        toolWindow.show()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = isEligibleForScanning(listTargets(event)) && isReadyToScan(
            event.project ?: return
        )
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    protected open fun listTargets(event: AnActionEvent): List<VirtualFile>? {
        return event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.asList()
    }

    private fun isEligibleForScanning(targets: List<VirtualFile>?): Boolean {
        return targets?.isNotEmpty() == true && targets.map { isEligible(it) }.all { it }
    }

    private fun isEligible(virtualFile: VirtualFile): Boolean {
        return virtualFile.fileType in SUPPORTED_FILE_TYPES || virtualFile.isDirectory
    }

    companion object {
        const val ID = "ScanWithMypyAction"
    }
}