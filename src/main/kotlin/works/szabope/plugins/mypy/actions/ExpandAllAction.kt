package works.szabope.plugins.mypy.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class ExpandAllAction : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        getPanel(event.project ?: return).expandAll()
    }
}
