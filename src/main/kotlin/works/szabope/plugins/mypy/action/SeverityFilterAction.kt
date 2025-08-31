package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.common.services.SeverityConfig
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

class SeverityFilterAction(private val config: SeverityConfig) :
    DumbAwareToggleAction(config.text, config.description, config.icon) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun isSelected(event: AnActionEvent): Boolean {
        val project = event.project ?: return true
        return MypyTreeService.getInstance(project).isSeverityLevelDisplayed(config.level)
    }

    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        val project = requireNotNull(event.project) {
            MypyBundle.message("mypy.please_report_this_issue")
        }
        MypyTreeService.getInstance(project).setSeverityLevelDisplayed(config.level, selected)
    }

    @VisibleForTesting
    fun getSeverity() = config.level
}