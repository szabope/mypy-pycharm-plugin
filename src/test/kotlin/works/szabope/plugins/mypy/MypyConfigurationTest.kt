package works.szabope.plugins.mypy

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData")
class MypyConfigurationTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/testData"

    fun testObsoleteVersion() {
        val pathToObsoleteMypy = Paths.get(myFixture.testDataPath).resolve("mypy_obsolete").absolutePathString()
        assertThrows(MypySettings.SettingsValidationException::class.java) {
            MypySettings.getInstance(myFixture.project).mypyExecutable = pathToObsoleteMypy
        }
    }
}