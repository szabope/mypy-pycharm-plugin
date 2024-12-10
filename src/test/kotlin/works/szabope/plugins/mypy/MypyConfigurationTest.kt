package works.szabope.plugins.mypy

import com.intellij.configurationStore.deserializeState
import com.intellij.openapi.components.ComponentManagerEx
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.waitUntil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import works.szabope.plugins.mypy.activity.MypySettingsInitializationActivity
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/configuration")
class MypyConfigurationTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/testData/configuration"

    @Ignore
    @Suppress("UnstableApiUsage")
    fun testInitializeFromOldMypySettings() {
        myFixture.copyFileToProject("mypy.conf")
        val oldMypyStateXml = JDOMUtil.load(
            """<component name="MypyConfigService">
                   <option name="mypyArguments" value="--exclude \.pyi${'$'}" />
                   <option name="mypyConfigFilePath" value="${testDataPath}/mypy.conf" />
               </component>""".trimIndent()
        )
        val settings = MypySettings.getInstance(myFixture.project)
        with(settings) {
            mypyExecutable = null
            configFilePath = null
            arguments = null
            projectDirectory = null
        }
        val oldMypyState = deserializeState(oldMypyStateXml, OldMypySettings.OldMypySettingsState::class.java)
        val oldSettings = OldMypySettings.getInstance(project)
        oldSettings.loadState(oldMypyState!!)
        (project as ComponentManagerEx).getCoroutineScope().launch {
            MypySettingsInitializationActivity().execute(project)
        }
        with(settings) {
            runBlocking {
                waitUntil { configFilePath != null && arguments != null }
            }
            assertNull(mypyExecutable)
            assertEquals(configFilePath, oldSettings.mypyConfigFilePath)
            assertEquals(arguments, oldSettings.mypyArguments)
        }
    }

    fun testObsoleteVersionIsNotSet() {
        MypySettings.getInstance(project).mypyExecutable = null
        val pathToObsoleteMypy = Paths.get(myFixture.testDataPath).resolve("mypy_obsolete").absolutePathString()
        MypySettings.getInstance(project).mypyExecutable = pathToObsoleteMypy
        assertNull(MypySettings.getInstance(project).mypyExecutable)
    }
}