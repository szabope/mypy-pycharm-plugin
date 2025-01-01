package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypySettings

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
}