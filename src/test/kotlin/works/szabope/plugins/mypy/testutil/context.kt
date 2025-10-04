package works.szabope.plugins.mypy.testutil

import com.intellij.ide.ui.IdeUiService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

fun dataContext(
    project: Project, customizer: SimpleDataContext.Builder.() -> Unit
): DataContext {
    val panel = ToolWindowManager.getInstance(project)
        .getToolWindow(MypyToolWindowPanel.ID)!!.contentManager.contents.single().component as MypyToolWindowPanel
    val panelContext = IdeUiService.getInstance().createUiDataContext(panel)
    val testContext = SimpleDataContext.builder().setParent(panelContext).add(CommonDataKeys.PROJECT, project).build()
    val builder = SimpleDataContext.builder().setParent(testContext)
    builder.apply { }
    customizer(builder)
    return builder.build()
}
