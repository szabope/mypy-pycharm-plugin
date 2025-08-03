package works.szabope.plugins.mypy

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.python.sdk.pythonSdk
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import works.szabope.plugins.common.sdk.PythonMockSdk
import works.szabope.plugins.mypy.services.MypySettings

abstract class AbstractMypyTestCase : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        MypySettings.getInstance(project).reset()
    }

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }

    protected suspend fun triggerReconfiguration() {
        MypySettingsInitializationTestService.getInstance(project).triggerReconfiguration()
    }

    fun withMockSdk(path: String, action: (Sdk) -> Unit) {
        val mockSdk = PythonMockSdk.create(path)
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        project.pythonSdk = mockSdk
        module.pythonSdk = mockSdk
        try {
            action(mockSdk)
        } finally {
            project.pythonSdk = null
            module.pythonSdk = null
            runWriteActionAndWait {
                ProjectJdkTable.getInstance().removeJdk(mockSdk)
            }
        }
    }
}