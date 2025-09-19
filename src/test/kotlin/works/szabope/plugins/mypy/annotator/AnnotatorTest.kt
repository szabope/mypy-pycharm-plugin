package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.TestDataPath
import works.szabope.plugins.mypy.AbstractMypyTestCase
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/annotation")
class AnnotatorTest : AbstractMypyTestCase() {
    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MypyInspection())
    }

    fun `test MypyAnnotator does not fail with incomplete settings`() {
        with(MypySettings.getInstance(myFixture.project)) {
            executablePath = null
            useProjectSdk = false
        }
        myFixture.configureByText(
            "a.py", """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
        )
        assertEmpty(myFixture.filterAvailableIntentions(MypyBundle.message("mypy.intention.ignore.text")))
    }

    fun `test MypyAnnotator does not fail if mypy executable path has a space in it`() {
        with(MypySettings.getInstance(project)) {
            executablePath = Paths.get(testDataPath).resolve("white space/mypy").absolutePathString()
            configFilePath = Paths.get(testDataPath).resolve("white space/mypy.ini").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = null
            useProjectSdk = false
        }
        myFixture.configureByText(
            "a.py", """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
        )
        val a = myFixture.doHighlighting().filter { it.toolId == MypyAnnotator::class.java}
        assertEquals(1, a.size)
    }
}