package works.szabope.plugins.mypy

import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.replaceService
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.tree.TreeTestUtil
import works.szabope.plugins.mypy.services.mypySeverityConfigs
import works.szabope.plugins.mypy.testutil.TestToolWindowHeadlessManagerImpl
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowFactory
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import works.szabope.plugins.mypy.toolWindow.MypyTreeService

abstract class AbstractToolWindowTestCase : AbstractMypyTestCase() {

    protected lateinit var treeUtil: TreeTestUtil
    protected lateinit var toolWindowManager: TestToolWindowHeadlessManagerImpl

    override fun setUp() {
        super.setUp()
        toolWindowManager = TestToolWindowHeadlessManagerImpl(project)
        project.replaceService(ToolWindowManager::class.java, toolWindowManager, testRootDisposable)
        setUpToolWindow()
        val panel = ToolWindowManager.getInstance(project)
            .getToolWindow(MypyToolWindowPanel.ID)!!.contentManager.contents.single().component as MypyToolWindowPanel
        treeUtil = TreeTestUtil(panel.tree)
        // ensure severities are on default setting
        with(MypyTreeService.getInstance(project)) {
            mypySeverityConfigs.keys.forEach { assertTrue(isSeverityLevelDisplayed(it)) }
        }
    }

    override fun tearDown() {
        toolWindowManager.cleanup()
        super.tearDown()
    }

    private fun setUpToolWindow() {
        val toolWindowManager = ToolWindowManager.getInstance(project) as ToolWindowHeadlessManagerImpl
        val toolWindow = toolWindowManager.doRegisterToolWindow(MypyToolWindowPanel.ID)
        MypyToolWindowFactory().createToolWindowContent(myFixture.project, toolWindow)
    }
}
