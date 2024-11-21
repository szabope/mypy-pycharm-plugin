package works.szabope.plugins.mypy

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.annotator.MypyInspection
import works.szabope.plugins.mypy.services.MypySettings
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData")
class MypyAnnotationTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/testData"

    override fun setUp() {
        super.setUp()
        val pathToMypy = Paths.get(myFixture.testDataPath).resolve("mypy").absolutePathString()
        MypySettings.getInstance(myFixture.project).mypyExecutable = pathToMypy
        myFixture.enableInspections(MypyInspection())
    }

    fun testMypyAnnotation() {
        val testName = getTestName(true)
        myFixture.configureByFile("$testName.py")
        val highlightInfos = myFixture.doHighlighting()
        assertFalse(highlightInfos.isEmpty())
        val action = myFixture.findSingleIntention(MyBundle.message("mypy.intention.ignore.text"))
        myFixture.launchAction(action)
        myFixture.checkResultByFile("$testName.after.py", true)
    }
}
