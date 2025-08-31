@file:Suppress("ComponentNotRegistered") // see plugin.xml for details

package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import works.szabope.plugins.mypy.services.mypySeverityConfigs

class SeverityFiltersActionGroup : DumbAware, ActionGroup() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    lateinit var children: Array<out SeverityFilterAction>

    override fun getChildren(e: AnActionEvent?): Array<out SeverityFilterAction> {
        if (!::children.isInitialized) {
            children = mypySeverityConfigs.map { SeverityFilterAction(it.value) }.toTypedArray()
        }
        return children
    }

    companion object {
        const val ID = "works.szabope.plugins.mypy.ErrorLevelDisplayOptions.SeverityFilters"
    }
}