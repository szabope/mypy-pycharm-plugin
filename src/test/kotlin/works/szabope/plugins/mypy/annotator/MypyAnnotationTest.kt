package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

@TestDataPath("/testData/annotation")
class MypyAnnotationTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MypyInspection())
        with(MypySettings.getInstance(myFixture.project)) {
            mypyExecutable = Paths.get(myFixture.testDataPath).resolve("mypy").absolutePathString()
            projectDirectory = Paths.get(myFixture.testDataPath).pathString
        }
    }

    fun `test function annotated`() {
        myFixture.configureByText(
            "a.py", """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
        )
        val intention = myFixture.findSingleIntention(MyBundle.message("mypy.intention.ignore.text"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore 
               |   return 'fun'
               |""".trimMargin()
        )
    }

    fun `test triple-quoted string annotated, but no intention available`() {
        myFixture.configureByText(
            "c.py", """|def more_fun_here() -> str:
                                |   return <caret>f""${'"'}this one here {x}
                                |   should be annotated, but
                                |   intention should not be available""${'"'}""".trimMargin()
        )
        assertNotEmpty(myFixture.doHighlighting())
        assertEmpty(myFixture.filterAvailableIntentions(MyBundle.message("mypy.intention.ignore.text")))
    }
}
