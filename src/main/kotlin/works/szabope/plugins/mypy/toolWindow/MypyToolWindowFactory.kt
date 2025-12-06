package works.szabope.plugins.mypy.toolWindow

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.toolWindow.MyToolWindowFactory
import works.szabope.plugins.mypy.MypyBundle

class MypyToolWindowFactory : MyToolWindowFactory(MypyBundle.message("mypy.toolwindow.name")) {
    override fun createPanel(project: Project) = MypyToolWindowPanel(project)
}