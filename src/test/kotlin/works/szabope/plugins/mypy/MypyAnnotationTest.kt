package works.szabope.plugins.mypy

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import works.szabope.plugins.mypy.annotator.MypyInspection
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
    }

    fun testMypyAnnotation() {
        with(MypySettings.getInstance(myFixture.project)) {
            mypyExecutable = Paths.get(myFixture.testDataPath).resolve("mypy").absolutePathString()
            projectDirectory = Paths.get(myFixture.testDataPath).pathString
        }
        val intention = myFixture.getAvailableIntention(MyBundle.message("mypy.intention.ignore.text"), TEST_FILE)
        assertNotNull(intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResultByFile(TEST_FILE_ANNOTATED, true)
    }

    fun testMypyAnnotationWithIncompleteConfiguration() {
        MypySettings.getInstance(myFixture.project).mypyExecutable = null
        val intention = myFixture.getAvailableIntention(MyBundle.message("mypy.intention.ignore.text"), TEST_FILE)
        assertNull(intention)
    }

    companion object {
        const val TEST_FILE = "mypyAnnotation.py"
        const val TEST_FILE_ANNOTATED = "mypyAnnotation.after.py"
    }
}
