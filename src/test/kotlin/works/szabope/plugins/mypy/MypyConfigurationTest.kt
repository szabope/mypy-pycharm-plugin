package works.szabope.plugins.mypy

import com.intellij.configurationStore.deserializeState
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.services.OldMypySettings
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/configuration")
class MypyConfigurationTest : AbstractToolWindowTestCase() {

    override fun getTestDataPath() = "src/test/testData/configuration"

    @Suppress("UnstableApiUsage")
    fun testInitializeFromOldMypySettings() {
        myFixture.copyFileToProject("mypy.conf")
        val oldMypyStateXml = JDOMUtil.load(
            """<component name="MypyConfigService">
                   <option name="mypyArguments" value="--exclude \.pyi${'$'}" />
                   <option name="mypyConfigFilePath" value="${testDataPath}/mypy.conf" />
               </component>""".trimIndent()
        )
        val oldMypyState = deserializeState(oldMypyStateXml, OldMypySettings.OldMypySettingsState::class.java)
        val oldSettings = OldMypySettings.getInstance(project)
        val settings = MypySettings.getInstance(myFixture.project)
        settings.reset()
        oldSettings.loadState(oldMypyState!!)
        try {
            runBlocking { triggerReconfiguration() }
            with(settings) {
                assertNull(mypyExecutable)
                assertEquals(oldSettings.mypyConfigFilePath, configFilePath)
                assertEquals(oldSettings.mypyArguments, arguments)
            }
        } finally {
            oldSettings.reset()
        }
    }

    fun testObsoleteVersionIsNotSet() {
        MypySettings.getInstance(project).mypyExecutable = null
        val pathToObsoleteMypy = Paths.get(myFixture.testDataPath).resolve("mypy_obsolete").absolutePathString()
        MypySettings.getInstance(project).mypyExecutable = pathToObsoleteMypy
        assertNull(MypySettings.getInstance(project).mypyExecutable)
    }

    fun testConfigFileNotExist() {
        assertFalse(File("THIS-FILE-SHOULD-NOT-EXIST").exists())
        with(MypySettings.getInstance(project)) {
            {
                configFilePath = null
                configFilePath = "THIS-FILE-SHOULD-NOT-EXIST"
                assertNull(configFilePath)
            }
        }
    }

    fun testConfigFileIsADirectory() {
        myFixture.copyDirectoryToProject("dummy_dir", "/")
        with(MypySettings.getInstance(project)) {
            {
                configFilePath = null
                configFilePath = "/dummy_dir"
                assertNull(configFilePath)
            }
        }
    }

    fun testProjectDirectoryIsAFile() {
        myFixture.copyFileToProject("dummy")
        with(MypySettings.getInstance(project)) {
            projectDirectory = null
            projectDirectory = "dummy"
            assertNull(projectDirectory)
        }
    }
//
//    fun testEnsureValid() {
//        TODO()
//    }
//
//    fun testIsComplete() {
//        TODO()
//    }
}
