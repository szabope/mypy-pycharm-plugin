package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.AssertionFailedError
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.mypy.AbstractToolWindowTestCase
import works.szabope.plugins.mypy.action.ScanActionUtil.isReadyToScan
import works.szabope.plugins.mypy.dialog.DialogManager
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.testutil.*
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/scan_sdk")
class ScanSdkTest : AbstractToolWindowTestCase() {

    private val dialogManager = TestDialogManager()

    override fun getTestDataPath() = "src/test/testData/action/scan_sdk"

    override fun setUp() {
        mockkObject(DialogManager.Companion)
        every { DialogManager.dialogManager } answers { dialogManager }
        super.setUp()
    }

    @Suppress("removal")
    fun testManualScan() = withMockSdk("${Paths.get(testDataPath).absolutePathString()}/MockSdk") {
        myFixture.copyDirectoryToProject("/", "/")
        installMypy(with(project) { getProjectContext() })
        setUpSettings()
        val excludedDir = TempFileSystem.getInstance().findFileByPath("/src/excluded_dir")!!
        val exclusionContext = with(project) {
            getContext { it.add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(excludedDir)) }
        }
        markExcluded(exclusionContext)
        var assertionError: Error? = null
        toolWindowManager.onBalloon {
            assertionError = AssertionFailedError("Should not happen")
        }
        val target = TempFileSystem.getInstance().findFileByPath("/src")!!
        scan(with(project) { getContext { it.add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(target)) } })
        PlatformTestUtil.waitWhileBusy { !isReadyToScan(project) }
        assertionError?.let { throw it }
        runBlocking {
            waitUntilAssertSucceeds {
                treeUtil.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
            }.also {
                treeUtil.expandAll()
                treeUtil.assertStructure(
                    """|-Found 1 issue(s) in 1 file(s)
                   | -src/a.py
                   |  Bracketed expression "[...]" is not valid as a type [valid-type] (0:-1) Did you mean "List[...]"?
                   |""".trimMargin()
                )
            }
        }
        unmark(exclusionContext)
    }

    private fun setUpSettings() {
        with(MypySettings.getInstance(project)) {
            executablePath = ""
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            useProjectSdk = true
            configFilePath = ""
            scanBeforeCheckIn = false
            arguments = ""
            excludeNonProjectFiles = true
        }
    }
}
