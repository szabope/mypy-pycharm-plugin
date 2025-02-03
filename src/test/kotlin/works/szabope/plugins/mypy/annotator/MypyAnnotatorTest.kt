package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/testData/annotation")
class MypyAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MypyInspection())
    }

    fun `test MypyAnnotator does not fail with incomplete settings`() {
        MypySettings.getInstance(myFixture.project).mypyExecutable = null
        myFixture.configureByText(
            "a.py", """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
        )
        assertEmpty(myFixture.filterAvailableIntentions(MyBundle.message("mypy.intention.ignore.text")))
    }

    fun `test MypyAnnotator does not fail if mypy executable path has a space in it`() {
        with(MypySettings.getInstance(project)) {
            mypyExecutable = Paths.get(testDataPath).resolve("white space/mypy").absolutePathString()
            projectDirectory = Paths.get(testDataPath).pathString
        }
        myFixture.configureByText(
            "a.py", """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
        )
        assertNotEmpty(myFixture.filterAvailableIntentions(MyBundle.message("mypy.intention.ignore.text")))
    }
}