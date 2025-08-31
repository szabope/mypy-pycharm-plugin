package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import works.szabope.plugins.mypy.services.AsyncScanService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.tool.MypyPublishingToolOutputHandler
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

open class ScanAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val targets = listTargets(event) ?: return
        val project = event.project ?: return
        MypyTreeService.getInstance(project).reinitialize(targets)
        AsyncScanService.getInstance(project).scan(
            targets, MypySettings.getInstance(project).getData(), MypyPublishingToolOutputHandler(project)
        )
        ToolWindowManager.getInstance(project).getToolWindow(MypyToolWindowPanel.ID)?.show()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled =
            isEligibleForScanning(listTargets(event)) && event.project?.let { ScanActionUtil.isReadyToScan(it) } == true
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
        const val ID = "works.szabope.plugins.mypy.action.ScanAction"
    }
}
