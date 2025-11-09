package works.szabope.plugins.mypy.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import works.szabope.plugins.mypy.AbstractToolWindowTestCase
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.testutil.dataContext
import works.szabope.plugins.mypy.testutil.scan
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/action/rescan")
class RescanTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/action/rescan"

    override fun setUp() {
        super.setUp()
        with(MypySettings.getInstance(project)) {
            useProjectSdk = false
            executablePath = Paths.get(testDataPath).resolve("mypy").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
        }
        val file = myFixture.configureByText("a.py", "doesn't matter").virtualFile
        scan(dataContext(project) { add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(file)) })
        PlatformTestUtil.waitWhileBusy { ScanJobRegistry.INSTANCE.isActive() }
    }

    /**
     * To be sure that rescan was actually called, we reconfigure executable to a version that returns mypy results: `mypy2`
     * `mypy` executable returns no results
     */
    fun `test rescan running for the same file scan did`() {
        MypySettings.getInstance(project).executablePath = Paths.get(testDataPath).resolve("mypy2").absolutePathString()
        PlatformTestUtil.invokeNamedAction(RescanAction.ID)
        PlatformTestUtil.waitWhileBusy { ScanJobRegistry.INSTANCE.isActive() }
        treeUtil.assertStructure("+Found 1 issue(s) in 1 file(s)\n")
        treeUtil.expandAll()
        treeUtil.assertStructure(
            """|-Found 1 issue(s) in 1 file(s)
                   | -src/a.py
                   |  Bracketed expression "[...]" is not valid as a type [valid-type] (0:-1) Did you mean "List[...]"?
                   |""".trimMargin()
        )
    }
}