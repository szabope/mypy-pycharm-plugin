package works.szabope.plugins.mypy.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel.Companion.MYPY_SEVERITY_FILTER_VALUES

class SeverityFiltersActionGroup : DumbAware, ActionGroup() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?) =
        MYPY_SEVERITY_FILTER_VALUES.map { SeverityFilterAction(createConfigFor(it)) }.toTypedArray()

    private fun createConfigFor(severity: String): SeverityFilterActionConfig {
        return when (severity) {
            "ERROR" -> SeverityFilterActionConfig(
                severity,
                MyBundle.message("action.MypyDisplayErrorsAction.text"),
                MyBundle.message("action.MypyDisplayErrorsAction.description"),
                AllIcons.General.Error
            )

            "WARNING" -> SeverityFilterActionConfig(
                severity,
                MyBundle.message("action.MypyDisplayWarningsAction.text"),
                MyBundle.message("action.MypyDisplayWarningsAction.description"),
                AllIcons.General.Warning
            )

            "NOTE" -> SeverityFilterActionConfig(
                severity,
                MyBundle.message("action.MypyDisplayNoteAction.text"),
                MyBundle.message("action.MypyDisplayNoteAction.description"),
                AllIcons.General.Information
            )

            else -> throw IllegalArgumentException("Unknown severity $severity")
        }
    }
}