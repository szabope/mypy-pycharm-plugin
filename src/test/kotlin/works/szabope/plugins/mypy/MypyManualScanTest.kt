package works.szabope.plugins.mypy

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.waitUntil
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.tree.TreeTestUtil
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import works.szabope.plugins.mypy.actions.ScanWithMypyAction
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowFactory
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData")
class MypyManualScanTest : BasePlatformTestCase() {

    private val tree: Tree = Tree()

    override fun getTestDataPath() = "src/test/testData"

    override fun setUp() {
        super.setUp()
        setUpSettings()
        setUpMypyToolWindow()
    }

    fun testManualScan() = runBlocking {
        val testName = getTestName(true)
        val file = myFixture.configureByFile("$testName.py")
        scan(file)
        val util = TreeTestUtil(tree)
        waitUntil {
            try {
                util.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
            } catch (e: AssertionError) {
                return@waitUntil false
            }
            true
        }.also {
            util.expandAll()
            util.assertStructure(
                "-Found 1 issue(s) in 1 file(s)\n" +
                        " -/src/manualScan.py\n" +
                        "  Bracketed expression \"[...]\" is not valid as a type [valid-type] (0:-1) Did you mean \"List[...]\"?\n"
            )
        }
    }

    @Suppress("UnstableApiUsage")
    private fun scan(file: PsiFile) {
        val action = ActionUtil.getAction(ScanWithMypyAction.ID)!!
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, myFixture.project)
            .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file.virtualFile)).build()
        val actionEvent = AnActionEvent.createFromDataContext(ActionPlaces.EDITOR_TAB, null, dataContext)
        action.update(actionEvent)
        Assert.assertTrue(actionEvent.presentation.isEnabled)
        action.actionPerformed(actionEvent)
    }

    private fun setUpSettings() {
        val pathToMypy = Paths.get(myFixture.testDataPath).resolve("mypy").absolutePathString()
        MypySettings.getInstance(myFixture.project).mypyExecutable = pathToMypy
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