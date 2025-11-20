package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.TestDataPath
import junit.framework.AssertionFailedError
import works.szabope.plugins.mypy.AbstractToolWindowTestCase
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/annotation")
class AnnotatorTest : AbstractToolWindowTestCase() {
    companion object {
        val DOESNT_MATTER = """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
    }

    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MypyInspection())
    }

    fun `test MypyAnnotator does not fail with incomplete settings`() {
        with(MypySettings.getInstance(project)) {
            executablePath = ""
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", DOESNT_MATTER)
        assertEmpty(myFixture.filterAvailableIntentions("Suppress mypy "))
    }

    fun `test MypyAnnotator does not fail if mypy executable path has a space in it`() {
        with(MypySettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve("white space/mypy").absolutePathString()
            configFilePath = Paths.get(testDataPath).resolve("white space/mypy.ini").absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = ""
            useProjectSdk = false
        }
        myFixture.configureByText("a.py", DOESNT_MATTER)
        @Suppress("UnstableApiUsage") val mypyAnnotations =
            myFixture.doHighlighting().filter { it.toolId == MypyAnnotator::class.java }
        assertEquals(1, mypyAnnotations.size)
    }

    fun `test MypyAnnotator does not run for in-memory target`() {
        with(MypySettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve("does_not_exist").absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = ""
            useProjectSdk = false
        }
        var assertionError: Error? = null
        toolWindowManager.onBalloon {
            assertionError = AssertionFailedError("Should not happen: $it")
        }
        val inMemoryTarget = LightVirtualFile("file-in-memory.py", "")
        myFixture.configureFromExistingVirtualFile(inMemoryTarget)
        @Suppress("UnstableApiUsage") val mypyAnnotations =
            myFixture.doHighlighting().filter { it.toolId == MypyAnnotator::class.java }
        assertEquals(0, mypyAnnotations.size)
        assertionError?.let { throw it }
    }
}