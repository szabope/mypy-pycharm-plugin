@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.mypy.initialization

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.testFramework.PlatformTestUtil
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.sdk.PyRemoteSdkAdditionalDataMarker
import com.jetbrains.python.sdk.PythonSdkAdditionalData
import com.jetbrains.python.sdk.flavors.PyFlavorAndData
import com.jetbrains.python.sdk.flavors.PyFlavorData
import com.jetbrains.python.sdk.flavors.PythonSdkFlavor
import com.jetbrains.python.sdk.pythonSdk
import io.mockk.every
import io.mockk.mockkObject
import works.szabope.plugins.common.test.sdk.PythonMockSdk
import works.szabope.plugins.mypy.AbstractMypyHeavyPlatformTestCase
import works.szabope.plugins.mypy.testutil.getMypyConfigurationNotCompleteNotification
import java.nio.file.Path
import java.nio.file.Paths

class MypyInitializationWithRemotePythonSdkTest : AbstractMypyHeavyPlatformTestCase() {

    private class RemoteSdkAdditionalData(workingDirectory: Path) : PythonSdkAdditionalData(
        PyFlavorAndData(
            PyFlavorData.Empty,
            PythonSdkFlavor.UnknownFlavor.INSTANCE
        ), workingDirectory
    ), PyRemoteSdkAdditionalDataMarker

    override fun tearDown() {
        val mockSdk = module.pythonSdk!!
        module?.pythonSdk = null
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().removeJdk(mockSdk)
        }
        super.tearDown()
    }

    override fun setUpProject() {
        val mockSdk = PythonMockSdk.create(
            "Remote Python 3.13.1 Docker (python:latest) (3)", "$PROJECT_PATH/MockSdk", LanguageLevel.PYTHON313
        )
        // let's lie about locality, see com.jetbrains.python.sdk.PythonSdkUtil#isRemote(Sdk)
        val remoteSdkAdditionalData = RemoteSdkAdditionalData(Paths.get("$PROJECT_PATH/MockSdk"))
        mockkObject(mockSdk)
        every { mockSdk.sdkAdditionalData } returns remoteSdkAdditionalData
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        myProject = PlatformTestUtil.loadAndOpenProject(Path.of(PROJECT_PATH).toAbsolutePath(), getTestRootDisposable())
        runWriteActionAndWait {
            myModule = ModuleManager.getInstance(myProject).modules.first()
        }
    }

    fun `test plugin initialized for project with python sdk results in notification`() {
        val actions = getMypyConfigurationNotCompleteNotification(project).actions
        assertEquals(1, actions.size)
    }

    companion object {
        const val PROJECT_PATH = "src/test/testData/initialization/projectWithRemoteSdk"
    }
}
