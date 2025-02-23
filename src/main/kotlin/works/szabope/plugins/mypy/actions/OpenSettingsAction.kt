package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.mypy.configurable.MypySettingConfigurable

class OpenSettingsAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ToolWindowManager.getInstance(e.project ?: return).invokeLater {
            ShowSettingsUtil.getInstance().showSettingsDialog(
                e.project,
                MypySettingConfigurable::class.java
            )
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.action.OpenSettingsAction"
    }
}