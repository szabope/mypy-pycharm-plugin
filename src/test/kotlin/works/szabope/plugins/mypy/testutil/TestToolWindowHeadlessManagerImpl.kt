package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import org.junit.Assert.assertNull

class TestToolWindowHeadlessManagerImpl(project: Project) : ToolWindowHeadlessManagerImpl(project) {
    private val myHandlers = hashMapOf<String, (ToolWindowBalloonShowOptions) -> Unit>()

    override fun notifyByBalloon(options: ToolWindowBalloonShowOptions) {
        myHandlers[options.toolWindowId]?.invoke(options)
    }

    fun onBalloon(toolWindowId: String, handler: (ToolWindowBalloonShowOptions) -> Unit) {
        assertNull(myHandlers.put(toolWindowId, handler))
    }

    fun cleanup() {
        myHandlers.clear()
    }
}
