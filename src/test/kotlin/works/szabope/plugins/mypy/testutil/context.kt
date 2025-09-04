package works.szabope.plugins.mypy.testutil

import com.intellij.ide.ui.IdeUiService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

context(project: Project) fun getProjectContext(
    customizer: ((SimpleDataContext.Builder) -> SimpleDataContext.Builder)? = null
): DataContext {
    val testContext = buildTestContext()
    val builder = SimpleDataContext.builder().setParent(testContext)
    builder.add(CommonDataKeys.PROJECT, project)
    customizer?.invoke(builder)
    return builder.build()
}

context(project: Project) fun getContext(
    customizer: ((SimpleDataContext.Builder) -> SimpleDataContext.Builder)? = null
): DataContext {
    val testContext = buildTestContext()
    val builder = SimpleDataContext.builder().setParent(testContext)
    customizer?.invoke(builder)
    return builder.build()
}

context(project: Project) private fun buildTestContext(): DataContext {
    val panel = ToolWindowManager.getInstance(project)
        .getToolWindow(MypyToolWindowPanel.ID)!!.contentManager.contents.single().component as MypyToolWindowPanel
    val panelContext = IdeUiService.getInstance().createUiDataContext(panel)
    return SimpleDataContext.builder().setParent(panelContext).add(CommonDataKeys.PROJECT, project).build()
}