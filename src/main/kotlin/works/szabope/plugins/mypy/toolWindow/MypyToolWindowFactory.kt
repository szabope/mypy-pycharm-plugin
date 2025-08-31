package works.szabope.plugins.mypy.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.ui.content.ContentFactory
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.mypy.MypyBundle


@VisibleForTesting
internal open class MypyToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MypyToolWindowPanel(project)
        val content =
            ContentFactory.getInstance().createContent(panel, MypyBundle.message("mypy.toolwindow.name"), false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setType(ToolWindowType.DOCKED, null)
    }
}
