package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.AssertionFailedError
import org.jetbrains.concurrency.asPromise
import works.szabope.plugins.common.test.dialog.TestDialogWrapper
import works.szabope.plugins.mypy.AbstractToolWindowTestCase
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.dialog.FailedToExecuteErrorDialog
import works.szabope.plugins.mypy.dialog.MypyExecutionErrorDialog
import works.szabope.plugins.mypy.dialog.MypyParseErrorDialog
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.testutil.*
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import javax.swing.event.HyperlinkEvent
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/scan_cli")
class ScanCliTest : AbstractToolWindowTestCase() {

    private val dialogManager = TestDialogManager()

    override fun getTestDataPath() = "src/test/testData/action/scan_cli"

    override fun setUp() {
        mockkObject(DialogManager.Companion)
        every { DialogManager.dialogManager } answers { dialogManager }
        super.setUp()
    }

    @Suppress("removal")
    fun testManualScan() {
        myFixture.copyDirectoryToProject("/", "/")
        val excludedDir = TempFileSystem.getInstance().findFileByPath("/src/excluded_dir")!!
        setUpSettings("mypy")
        val exclusionContext = dataContext(project) {
            add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(excludedDir))
        }
        markExcluded(exclusionContext)
        var assertionError: Error? = null
        toolWindowManager.onBalloon {
            assertionError = AssertionFailedError("Should not happen: $it")
        }
        val target = TempFileSystem.getInstance().findFileByPath("/src")!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.waitWhileBusy { !ScanActionUtil.isReadyToScan(project) }
        assertionError?.let { throw it }
        treeUtil.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
        treeUtil.expandAll()
        treeUtil.assertStructure(
            """|-Found 1 issue(s) in 1 file(s)
                   | -src/a.py
                   |  Bracketed expression "[...]" is not valid as a type [valid-type] (0:-1) Did you mean "List[...]"?
                   |""".trimMargin()
        )
        unmark(exclusionContext)
    }

    fun `test mypy returning non-json result with exit code 0 results in Dialog of The Parse Error`() {
        myFixture.copyDirectoryToProject("/", "/")
        setUpSettings("mypy_non_json_output")
        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URI("http://localhost").toURL()
                )
            )
        }
        val dialogShown = CompletableFuture<TestDialogWrapper>()
        dialogManager.onDialog(MypyParseErrorDialog::class.java) {
            dialogShown.complete(it)
            DialogWrapper.OK_EXIT_CODE
        }
        val target = WorkspaceModel.getInstance(project).currentSnapshot.entities(ContentRootEntity::class.java)
            .first().url.virtualFile!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.assertPromiseSucceeds(dialogShown.asPromise())
        assertTrue(dialogShown.isDone && with(dialogShown.get()) { isShown() && getExitCode() == DialogWrapper.OK_EXIT_CODE })
    }

    // https://github.com/python/mypy/issues/6003
    fun `test mypy returning with an exit code 1 is fine`() {
        myFixture.copyDirectoryToProject("/", "/")
        setUpSettings("mypy_exit_with_1")
        var assertionError: Error? = null
        toolWindowManager.onBalloon {
            assertionError = AssertionFailedError("Should not happen")
        }
        val target = WorkspaceModel.getInstance(project).currentSnapshot.entities(ContentRootEntity::class.java)
            .first().url.virtualFile!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.waitWhileBusy { !ScanActionUtil.isReadyToScan(project) }
        assertionError?.let { throw it }
    }

    fun `test mypy returning with an exit code 2 results in dialog`() {
        myFixture.copyDirectoryToProject("/", "/")
        setUpSettings("mypy_exit_with_2")
        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URI("http://localhost").toURL()
                )
            )
        }
        val dialogShown = CompletableFuture<TestDialogWrapper>()
        dialogManager.onDialog(MypyExecutionErrorDialog::class.java) {
            dialogShown.complete(it)
            DialogWrapper.OK_EXIT_CODE
        }
        val target = WorkspaceModel.getInstance(project).currentSnapshot.entities(ContentRootEntity::class.java)
            .first().url.virtualFile!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.assertPromiseSucceeds(dialogShown.asPromise())
        assertTrue(dialogShown.isDone && with(dialogShown.get()) { isShown() && getExitCode() == DialogWrapper.OK_EXIT_CODE })
    }

    fun `test executable path does not exist results in dialog`() {
        myFixture.copyDirectoryToProject("/", "/")
        with(MypySettings.getInstance(project)) {
            executablePath = "/does/not/exist"
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = false
            configFilePath = ""
            scanBeforeCheckIn = false
            arguments = ""
            excludeNonProjectFiles = true
        }
        toolWindowManager.onBalloon {
            it.listener?.hyperlinkUpdate(
                HyperlinkEvent(
                    "dumb", HyperlinkEvent.EventType.ACTIVATED, URI("http://localhost").toURL()
                )
            )
        }
        val dialogShown = CompletableFuture<TestDialogWrapper>()
        dialogManager.onDialog(FailedToExecuteErrorDialog::class.java) {
            dialogShown.complete(it)
            DialogWrapper.OK_EXIT_CODE
        }
        val target = WorkspaceModel.getInstance(project).currentSnapshot.entities(ContentRootEntity::class.java)
            .first().url.virtualFile!!
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) })
        PlatformTestUtil.assertPromiseSucceeds(dialogShown.asPromise())
        assertTrue(dialogShown.isDone && with(dialogShown.get()) { isShown() && getExitCode() == DialogWrapper.OK_EXIT_CODE })
    }

    private fun setUpSettings(executable: String) {
        with(MypySettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve(executable).absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = false
            configFilePath = ""
            scanBeforeCheckIn = false
            arguments = ""
            excludeNonProjectFiles = true
        }
    }
}
