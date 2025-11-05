package works.szabope.plugins.mypy.initialization

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.testFramework.PlatformTestUtil
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.test.sdk.PythonMockSdk
import works.szabope.plugins.mypy.AbstractMypyHeavyPlatformTestCase
import works.szabope.plugins.mypy.testutil.getMypyConfigurationNotCompleteNotification
import java.nio.file.Path

class MypyInitializationWithLocalPythonSdkTest : AbstractMypyHeavyPlatformTestCase() {

    override fun tearDown() {
        val mockSdk = project.pythonSdk!!
        project.pythonSdk = null
        module?.pythonSdk = null
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().removeJdk(mockSdk)
        }
        super.tearDown()
    }

    override fun setUpProject() {
        val mockSdk = PythonMockSdk.create("Python 3.12", "$PROJECT_PATH/MockSdk", LanguageLevel.PYTHON312)
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        myProject = PlatformTestUtil.loadAndOpenProject(Path.of(PROJECT_PATH), getTestRootDisposable())
    }

    fun `test plugin initialized for project with python sdk results in notification`() {
        val actions = getMypyConfigurationNotCompleteNotification(project).actions
        assertEquals(2, actions.size)
    }

    companion object {
        const val PROJECT_PATH = "src/test/testData/initialization/projectWithLocalSdk"
    }
}
