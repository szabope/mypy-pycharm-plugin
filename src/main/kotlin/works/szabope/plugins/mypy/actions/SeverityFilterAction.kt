package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import javax.swing.Icon

data class SeverityFilterActionConfig(val level: String, val text: String, val description: String, val icon: Icon)

class SeverityFilterAction(private val config: SeverityFilterActionConfig) :
    DumbAwareToggleAction(config.text, config.description, config.icon) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun isSelected(event: AnActionEvent): Boolean {
        return event.getData(MypyToolWindowPanel.MYPY_PANEL_DATA_KEY)?.isSeverityLevelDisplayed(config.level) ?: true
    }

    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        event.getData(MypyToolWindowPanel.MYPY_PANEL_DATA_KEY)?.setSeverityLevelDisplayed(config.level, selected)
    }
}