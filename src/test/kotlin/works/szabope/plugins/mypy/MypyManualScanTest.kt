package works.szabope.plugins.mypy

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.replaceService
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.tree.TreeTestUtil
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import works.szabope.plugins.mypy.actions.ScanWithMypyAction
import works.szabope.plugins.mypy.dialog.IDialogManager
import works.szabope.plugins.mypy.dialog.MypyDialog
import works.szabope.plugins.mypy.dialog.MypyExecutionErrorDialog
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowFactory
import works.szabope.plugins.mypy.toolWindow.MypyToolWindowPanel
import works.szabope.plugins.mypy.testutil.TestToolWindowHeadlessManagerImpl
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/testData/manualScan")
class MypyManualScanTest : BasePlatformTestCase() {

    private val tree: Tree = Tree()
    private lateinit var toolWindowManager: TestToolWindowHeadlessManagerImpl
    private lateinit var dialogManager: IDialogManager

    override fun getTestDataPath() = "src/test/testData/manualScan"

    override fun setUp() {
        super.setUp()
        toolWindowManager = TestToolWindowHeadlessManagerImpl(project)
        project.replaceService(ToolWindowManager::class.java, toolWindowManager, testRootDisposable)
        setUpMypyToolWindow()
        dialogManager = service<IDialogManager>()
    }

    override fun tearDown() {
        if (::dialogManager.isInitialized) dialogManager.cleanup()
        toolWindowManager.cleanup()
        super.tearDown()
    }

    fun testManualScan() = runBlocking {
        setUpSettings("mypy")
        val testName = getTestName(true)
        val file = myFixture.configureByFile("$testName.py")
        scan(file)
        val util = TreeTestUtil(tree)
        waitUntil {
            try {
                util.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
            } catch (ignored: AssertionError) {
                return@waitUntil false
            }
            true
        }.also {
            util.expandAll()
            util.assertStructure(
                "-Found 1 issue(s) in 1 file(s)\n" + " -/src/manualScan.py\n" + "  Bracketed expression \"[...]\" " + "is not valid as a type [valid-type] (0:-1) " + "Did you mean \"List[...]\"?\n"
            )
        }
    }

    fun testFailingScan() = runBlocking {
        toolWindowManager.onBalloon(MypyToolWindowPanel.ID) {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URL("http://localhost")
                )
            )
        }
        val dialogShown = CompletableFuture<MypyDialog>()
        dialogManager.onDialog(MypyExecutionErrorDialog::class.java) {
            dialogShown.complete(it)
            DialogWrapper.OK_EXIT_CODE
        }
        setUpSettings("mypy_failing")
        val file = myFixture.configureByFile("manualScan.py")
        scan(file)
        waitUntil {
            dialogShown.isDone && with(dialogShown.get()) { isShown() == true && getExitCode() == DialogWrapper.OK_EXIT_CODE }
        }
    }


    @Suppress("UnstableApiUsage")
    private fun scan(file: PsiFile) {
        val action = ActionUtil.getAction(ScanWithMypyAction.ID)!! as ScanWithMypyAction
        val dataContext = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, myFixture.project)
            .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file.virtualFile)).build()
        val actionEvent = AnActionEvent.createEvent(dataContext, null, ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
        action.update(actionEvent)
        Assert.assertTrue(actionEvent.presentation.isEnabled)
        action.actionPerformed(actionEvent)
    }

    private fun setUpSettings(executable: String) {
        with(MypySettings.getInstance(myFixture.project)) {
            mypyExecutable = Paths.get(myFixture.testDataPath).resolve(executable).absolutePathString()
            projectDirectory = Paths.get(myFixture.testDataPath).pathString
        }
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
