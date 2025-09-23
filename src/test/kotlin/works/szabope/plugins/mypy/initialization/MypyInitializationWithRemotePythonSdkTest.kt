package works.szabope.plugins.mypy.initialization

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.testFramework.PlatformTestUtil
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.remote.PyRemoteSdkAdditionalData
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.sdk.sdkFlavor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import works.szabope.plugins.common.test.sdk.PythonMockSdk
import works.szabope.plugins.mypy.AbstractMypyHeavyPlatformTestCase
import works.szabope.plugins.mypy.testutil.getMypyConfigurationNotCompleteNotification
import java.nio.file.Path

class MypyInitializationWithRemotePythonSdkTest : AbstractMypyHeavyPlatformTestCase() {

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
        val mockSdk = PythonMockSdk.create(
            "Remote Python 3.13.1 Docker (python:latest) (3)",
            "$PROJECT_PATH/MockSdk",
            LanguageLevel.PYTHON313
        )
        // let's lie about locality, see com.jetbrains.python.sdk.PythonSdkUtil#isRemote(Sdk)
        val mockAdditionalData = mockk<PyRemoteSdkAdditionalData>()
        every { mockAdditionalData.sdkId } returns "Python something"
        every { mockAdditionalData.flavor } returns mockSdk.sdkFlavor
        mockkObject(mockSdk)
        every { mockSdk.sdkAdditionalData } returns mockAdditionalData
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        myProject = PlatformTestUtil.loadAndOpenProject(Path.of(PROJECT_PATH), getTestRootDisposable())
    }

    fun `test plugin initialized for project with python sdk results in notification`() {
        val actions = with(project) { getMypyConfigurationNotCompleteNotification() }.actions
        assertEquals(1, actions.size)
    }

    companion object {
        const val PROJECT_PATH = "src/test/testData/initialization/projectWithRemoteSdk"
    }
}
