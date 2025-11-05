package works.szabope.plugins.mypy.initialization

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.util.ui.UIUtil
import works.szabope.plugins.mypy.AbstractMypyHeavyPlatformTestCase
import works.szabope.plugins.mypy.services.MypySettings
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute

class MypyInitializationFromOldConfigurationTest : AbstractMypyHeavyPlatformTestCase() {

    override fun setUpProject() {
        VfsRootAccess.allowRootAccess(testRootDisposable, "/usr/bin")
        thisLogger().info("Current dir: ${File(".").absolutePath}")
        thisLogger().info("Test data: ${Path.of(PROJECT_PATH).absolute()}")
        val testDataDir = Path.of(PROJECT_PATH).toFile()
        thisLogger().info("Test data exists: ${testDataDir.exists()}")
        myProject = PlatformTestUtil.loadAndOpenProject(Path.of(PROJECT_PATH), getTestRootDisposable())
    }

    fun `test plugin initialized from old configuration`() {
        UIUtil.dispatchAllInvocationEvents()
        DumbService.getInstance(project).waitForSmartMode()
        with(MypySettings.getInstance(project)) {
            assertFalse(useProjectSdk)
            assertEquals("$PROJECT_PATH/.venv/bin/mypy", executablePath)
            assertEquals("--show-column-numbers", arguments)
            assertFalse(scanBeforeCheckIn)
            assertEquals("$PROJECT_PATH/mypy.conf", configFilePath)
        }
    }

    companion object {
        const val PROJECT_PATH = "src/test/testData/initialization/OldConfiguration"
    }
}
