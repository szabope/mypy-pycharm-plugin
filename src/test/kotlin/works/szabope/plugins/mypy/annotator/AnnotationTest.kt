package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/annotation")
class AnnotationTest : BasePlatformTestCase() {

    companion object {
        val DOESNT_MATTER = """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
    }

    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MypyInspection())
        with(MypySettings.getInstance(myFixture.project)) {
            executablePath = Paths.get(myFixture.testDataPath).resolve("mypy").absolutePathString()
            projectDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = ""
            useProjectSdk = false
        }
    }

    fun `test function annotated`() {
        myFixture.configureByText(
            "a.py", """|def<caret> lets_have_fun() -> [int]:
                                |   return 'fun'
                                |""".trimMargin()
        )
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "valid-type"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore[valid-type] 
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
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "valid-type"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore[valid-type] # comment
               |   return 'fun'
               |""".trimMargin()
        )
        PsiTestUtil.checkFileStructure(myFixture.file)
    }

    fun `test existing ignore with codes gets extended`() {
        myFixture.configureByText(
            "b.py", """|def<caret> lets_have_fun() -> [int]:  # type: ignore[some-code,another-code, and-a-third-one]
                                |   return 'fun'
                                |""".trimMargin()
        )
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "valid-type"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore[some-code,another-code, and-a-third-one,valid-type] 
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
        assertEmpty(myFixture.filterAvailableIntentions(MypyBundle.message("mypy.intention.ignore.text", "name-defined")))
    }

    fun `test single line triple-quoted string annotated with intention available`() {
        myFixture.configureByText(
            "d.py", """|def more_fun_here() -> str:
                                |   return <caret>f""${'"'}this one here {x}""${'"'}""".trimMargin()
        )
        assertNotEmpty(myFixture.doHighlighting())
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "name-defined"))
        assertNotNull(intention)
    }

    fun `test annotations are processed even with mypy mixing non-json stuff into stdout`() {
        myFixture.configureByText("errorMixedIntoStdOut.py", DOESNT_MATTER)
        @Suppress("UnstableApiUsage") val mypyAnnotations =
            myFixture.doHighlighting().filter { it.toolId == MypyAnnotator::class.java }
        assertEquals(2, mypyAnnotations.size)
    }
}