package works.szabope.plugins.mypy

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.HeavyPlatformTestCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import works.szabope.plugins.common.services.PluginPackageManagementService
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.testutil.MypyPluginPackageManagementServiceStub

abstract class AbstractMypyHeavyPlatformTestCase : HeavyPlatformTestCase() {

    // local variables are not supported in mockk answer, yet
    private lateinit var mypyPackageManagementServiceStub: PluginPackageManagementService

    override fun setUp() {
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
    }

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }
}