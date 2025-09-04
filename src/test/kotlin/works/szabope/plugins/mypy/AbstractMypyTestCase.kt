package works.szabope.plugins.mypy

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.python.sdk.pythonSdk
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import works.szabope.plugins.common.services.PluginPackageManagementService
import works.szabope.plugins.common.test.sdk.PythonMockSdk
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.services.MypySettings
import works.szabope.plugins.mypy.testutil.MypyPluginPackageManagementServiceStub

abstract class AbstractMypyTestCase : BasePlatformTestCase() {

    // local variables are not supported in mockk answer, yet
    private lateinit var mypyPackageManagementServiceStub: PluginPackageManagementService

    override fun setUp() {
        // FIXME: this is a ducktape for
        //  com.intellij.python.community.services.systemPython.searchPythonsPhysicallyNoCache
        //  accessing /usr/bin/python3(\.\d+)? which is not allowed from tests
        VfsRootAccess.allowRootAccess(testRootDisposable, "/usr/bin")
        mockkObject(MypyPluginPackageManagementService.Companion)
        every { MypyPluginPackageManagementService.getInstance(any(Project::class)) } answers {
            if (!::mypyPackageManagementServiceStub.isInitialized) {
                mypyPackageManagementServiceStub = MypyPluginPackageManagementServiceStub(
                    firstArg<Project>()
                )
            }
            mypyPackageManagementServiceStub
        }
        super.setUp()
        MypySettings.getInstance(project).reset()
    }

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }

    /**
     * https://youtrack.jetbrains.com/issue/IJPL-197007
     */
    override fun getProjectDescriptor(): LightProjectDescriptor? {
        return LightProjectDescriptor()
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