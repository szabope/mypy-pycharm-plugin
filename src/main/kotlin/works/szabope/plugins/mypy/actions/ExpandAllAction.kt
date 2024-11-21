package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

class ExpandAllAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        event.getData(MypyToolWindowPanel.MYPY_PANEL_DATA_KEY)?.expandAll()
    }
}
