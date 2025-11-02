package works.szabope.plugins.mypy.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel.Companion.MYPY_SEVERITY_FILTER_VALUES

class SeverityFiltersActionGroup : DumbAware, ActionGroup() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?) =
        MYPY_SEVERITY_FILTER_VALUES.map { SeverityFilterAction(createConfigFor(it)) }.toTypedArray()

    private fun createConfigFor(severity: String): SeverityFilterActionConfig {
        return when (severity) {
            "ERROR" -> SeverityFilterActionConfig(
                severity,
                MypyBundle.message("action.MyPyDisplayErrorsAction.text"),
                MypyBundle.message("action.MyPyDisplayErrorsAction.description"),
                AllIcons.General.Error
            )

            "WARNING" -> SeverityFilterActionConfig(
                severity,
                MypyBundle.message("action.MypyDisplayWarningsAction.text"),
                MypyBundle.message("action.MypyDisplayWarningsAction.description"),
                AllIcons.General.Warning
            )

            "NOTE" -> SeverityFilterActionConfig(
                severity,
                MypyBundle.message("action.MypyDisplayNoteAction.text"),
                MypyBundle.message("action.MypyDisplayNoteAction.description"),
                AllIcons.General.Information
            )

            else -> throw IllegalArgumentException("Unknown severity $severity")
        }
    }
}