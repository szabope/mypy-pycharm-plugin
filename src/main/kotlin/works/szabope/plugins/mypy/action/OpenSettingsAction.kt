package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.mypy.configurable.MypyConfigurable
import works.szabope.plugins.mypy.services.ExecutableService
import works.szabope.plugins.mypy.services.MypySettings

class OpenSettingsAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        if (MypySettings.getInstance(project).executablePath == null) {
            ExecutableService.getInstance(project).findExecutable()?.let {
                MypySettings.getInstance(project).executablePath = it
            }
        }
        ToolWindowManager.getInstance(project).invokeLater {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, MypyConfigurable::class.java)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.OpenSettingsAction"
    }
}