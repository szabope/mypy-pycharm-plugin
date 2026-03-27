package works.szabope.plugins.mypy.testutil

import com.intellij.openapi.project.Project
import works.szabope.plugins.common.test.toolWindow.AbstractTestToolWindowHeadlessManagerImpl
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

class TestToolWindowHeadlessManagerImpl(project: Project) :
    AbstractTestToolWindowHeadlessManagerImpl(project) {

    override val toolWindowId = MypyToolWindowPanel.ID
}
