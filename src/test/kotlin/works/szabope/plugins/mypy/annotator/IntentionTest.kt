package works.szabope.plugins.mypy.annotator

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.MypyBundle
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/annotation")
class IntentionTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/testData/annotation"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MypyInspection())
        with(MypySettings.getInstance(myFixture.project)) {
            executablePath = Paths.get(myFixture.testDataPath).resolve("mypy").absolutePathString()
            workingDirectory = Paths.get(testDataPath).absolutePathString()
            arguments = ""
            useProjectSdk = false
        }
    }

    fun `test function annotated`() {
        myFixture.configureByFile("a.py")
        assertNotEmpty(myFixture.doHighlighting())
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "valid-type"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore[valid-type] 
               |    return 'fun'
               |""".trimMargin()
        )
        PsiTestUtil.checkFileStructure(myFixture.file)
    }

    fun `test function annotated with comment`() {
        myFixture.configureByFile("e.py")
        assertNotEmpty(myFixture.doHighlighting())
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "valid-type"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore[valid-type] # comment
               |    return 'fun'
               |""".trimMargin()
        )
        PsiTestUtil.checkFileStructure(myFixture.file)
    }

    fun `test existing ignore with codes gets extended`() {
        myFixture.configureByFile("b.py")
        assertNotEmpty(myFixture.doHighlighting())
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "valid-type"))
        assertNotNull(intention)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """|def lets_have_fun() -> [int]:  # type: ignore[some-code,another-code, and-a-third-one,valid-type] 
               |    return 'fun'
               |""".trimMargin()
        )
        PsiTestUtil.checkFileStructure(myFixture.file)
    }

    fun `test triple-quoted string annotated, but no intention available`() {
        myFixture.configureByFile("c.py")
        assertNotEmpty(myFixture.doHighlighting())
        assertEmpty(myFixture.filterAvailableIntentions("Suppress mypy "))
    }

    fun `test single line triple-quoted string annotated with intention available`() {
        myFixture.configureByFile("d.py")
        assertNotEmpty(myFixture.doHighlighting())
        val intention = myFixture.findSingleIntention(MypyBundle.message("mypy.intention.ignore.text", "name-defined"))
        assertNotNull(intention)
    }

    fun `test annotations are processed even with mypy mixing non-json stuff into stdout`() {
        myFixture.configureByFile("errorMixedIntoStdOut.py")
        @Suppress("UnstableApiUsage") val mypyAnnotations =
            myFixture.doHighlighting().filter { it.toolId == MypyAnnotator::class.java }
        assertEquals(2, mypyAnnotations.size)
    }

    fun `test annotations for anything but target are skipped`() {
        myFixture.configureByFiles("followsImports.py", "dummy.py")
        @Suppress("UnstableApiUsage") val mypyAnnotations =
            myFixture.doHighlighting().filter { it.toolId == MypyAnnotator::class.java }
        assertEquals(1, mypyAnnotations.size)
    }
}