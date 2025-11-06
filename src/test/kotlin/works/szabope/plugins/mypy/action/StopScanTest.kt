package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import works.szabope.plugins.mypy.AbstractToolWindowTestCase
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.testutil.dataContext
import works.szabope.plugins.mypy.testutil.scan
import works.szabope.plugins.mypy.testutil.stopScan
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/stop_scan")
class StopScanTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/action/stop_scan"

    override fun setUp() {
        super.setUp()
        with(MypySettings.getInstance(project)) {
            useProjectSdk = false
            executablePath = Paths.get(testDataPath).resolve("mypy").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
        }
    }

    /**
     * For the infinite loop check `mypy` shell script on testDataPath
     */
    fun `test that we can stop an external process that runs an infinite loop`() {
        val file = myFixture.configureByText("a.py", "doesn't matter").virtualFile
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)) })
        stopScan(dataContext(project) {})
        PlatformTestUtil.waitWhileBusy { ScanJobRegistry.INSTANCE.isActive() }
    }
}