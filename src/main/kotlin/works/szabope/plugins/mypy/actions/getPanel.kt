package works.szabope.plugins.mypy.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

fun getPanel(project: Project): MypyToolWindowPanel = ToolWindowManager.getInstance(project)
    .getToolWindow(MypyToolWindowPanel.ID)?.contentManager?.getContent(0)?.component as MypyToolWindowPanel
