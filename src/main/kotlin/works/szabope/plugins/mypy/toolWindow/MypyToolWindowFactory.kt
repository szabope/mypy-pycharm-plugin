package works.szabope.plugins.mypy.toolWindow

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.toolWindow.AbstractToolWindowFactory
import works.szabope.plugins.mypy.MypyBundle

class MypyToolWindowFactory : AbstractToolWindowFactory(MypyBundle.message("mypy.toolwindow.name")) {
    override fun createPanel(project: Project) = MypyToolWindowPanel(project)
}