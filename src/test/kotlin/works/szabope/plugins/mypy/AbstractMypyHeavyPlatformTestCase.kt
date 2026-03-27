package works.szabope.plugins.mypy

import com.intellij.openapi.project.Project
import com.intellij.testFramework.replaceService
import io.mockk.every
import io.mockk.mockkObject
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.test.AbstractPluginHeavyPlatformTestCase
import works.szabope.plugins.mypy.action.MypyScanJobRegistryService
import works.szabope.plugins.mypy.services.MypyPluginPackageManagementService
import works.szabope.plugins.mypy.testutil.MypyPluginPackageManagementServiceStub

abstract class AbstractMypyHeavyPlatformTestCase : AbstractPluginHeavyPlatformTestCase() {

    override fun setupPackageManagementServiceMock(stubProvider: (Project) -> AbstractPluginPackageManagementService) {
        mockkObject(MypyPluginPackageManagementService.Companion)
        every { MypyPluginPackageManagementService.getInstance(any(Project::class)) } answers {
            stubProvider(firstArg())
        }
    }

    override fun createPackageManagementServiceStub(project: Project) = MypyPluginPackageManagementServiceStub(project)

    override fun onSetUp() {
        project.replaceService(MypyScanJobRegistryService::class.java, MypyScanJobRegistryService(), testRootDisposable)
    }
}
