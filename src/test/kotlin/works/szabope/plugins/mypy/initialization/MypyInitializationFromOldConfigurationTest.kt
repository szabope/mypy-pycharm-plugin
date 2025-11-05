package works.szabope.plugins.mypy.initialization

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.PlatformTestUtil
import works.szabope.plugins.mypy.AbstractMypyHeavyPlatformTestCase
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Path

class MypyInitializationFromOldConfigurationTest : AbstractMypyHeavyPlatformTestCase() {

    override fun setUpProject() {
        VfsRootAccess.allowRootAccess(testRootDisposable, "/usr/bin")
        myProject = PlatformTestUtil.loadAndOpenProject(Path.of(PROJECT_PATH), getTestRootDisposable())
    }

    fun `test plugin initialized from old configuration`() {
        with(MypySettings.getInstance(project)) {
            PlatformTestUtil.waitWhileBusy { !isInitialized() }
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
