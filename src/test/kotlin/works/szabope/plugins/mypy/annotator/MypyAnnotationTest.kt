package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.MyBundle
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/testData/annotation")
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
        PsiTestUtil.checkFileStructure(myFixture.file)
    }

    fun `test function annotated with comment`() {
        myFixture.configureByText(
            "b.py", """|def<caret> lets_have_fun() -> [int]:  # comment
                                |   return 'fun'
                                |""".trimMargin()
        )
        val intention = myFixture.findSingleIntention(MyBundle.message("mypy.intention.ignore.text"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore # comment
               |   return 'fun'
               |""".trimMargin()
        )
        PsiTestUtil.checkFileStructure(myFixture.file)
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

    fun `test single line triple-quoted string annotated with intention available`() {
        myFixture.configureByText(
            "d.py", """|def more_fun_here() -> str:
                                |   return <caret>f""${'"'}this one here {x}""${'"'}""".trimMargin()
        )
        assertNotEmpty(myFixture.doHighlighting())
        val intention = myFixture.findSingleIntention(MyBundle.message("mypy.intention.ignore.text"))
        assertNotNull(intention)
    }
}
