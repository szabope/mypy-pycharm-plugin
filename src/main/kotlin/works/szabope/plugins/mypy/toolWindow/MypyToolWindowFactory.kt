package works.szabope.plugins.mypy.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.ui.content.ContentFactory
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.messages.IMypyScanResultListener
import works.szabope.plugins.mypy.messages.MypyScanResultPublisher


@VisibleForTesting
internal open class MypyToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = createPanel(project)
        project.messageBus.connect(toolWindow.disposable)
            .subscribe(MypyScanResultPublisher.SCAN_RESULT_TOPIC, IMypyScanResultListener {
                panel.addScanResult(it)
            })
        val content =
            ContentFactory.getInstance().createContent(panel, MypyBundle.message("mypy.toolwindow.name"), false)
        toolWindow.contentManager.addContent(content)
        toolWindow.setType(ToolWindowType.DOCKED, null)
    }

    @VisibleForTesting
    protected open fun createPanel(project: Project): MypyToolWindowPanel {
        return MypyToolWindowPanel(project)
    }
}
