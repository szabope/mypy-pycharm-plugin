package works.szabope.plugins.mypy

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.replaceService
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.treeStructure.Tree
import works.szabope.plugins.mypy.testutil.TestToolWindowHeadlessManagerImpl
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowFactory
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel

abstract class AbstractToolWindowTestCase : AbstractMypyTestCase() {

    protected val tree: Tree = Tree()
    protected lateinit var toolWindowManager: TestToolWindowHeadlessManagerImpl

    override fun setUp() {
        super.setUp()
        toolWindowManager = TestToolWindowHeadlessManagerImpl(project)
        project.replaceService(ToolWindowManager::class.java, toolWindowManager, testRootDisposable)
        setUpMypyToolWindow()
    }

    override fun tearDown() {
        toolWindowManager.cleanup()
        super.tearDown()
    }

    private fun setUpMypyToolWindow() {
        val toolWindowManager = ToolWindowManager.getInstance(project) as ToolWindowHeadlessManagerImpl
        val toolWindow = toolWindowManager.doRegisterToolWindow(MypyToolWindowPanel.ID)
        val factory = object : MypyToolWindowFactory() {
            override fun createPanel(project: Project): MypyToolWindowPanel {
                return MypyToolWindowPanel(project, tree)
            }
        }
        factory.createToolWindowContent(myFixture.project, toolWindow)
    }
}