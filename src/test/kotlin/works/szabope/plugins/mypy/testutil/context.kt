package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.test.context.dataContext
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

fun dataContext(
    project: Project, customizer: SimpleDataContext.Builder.() -> Unit
): DataContext = dataContext(project, MypyToolWindowPanel.ID, customizer)
