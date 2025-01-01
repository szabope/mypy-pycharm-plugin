package works.szabope.plugins.mypy

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking

abstract class MypyTestCase : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        MypySettingsInitializationTestService.getInstance(project).executeInitialization()
    }

    protected fun awaitProcessed(cb: () -> Unit) = runBlocking {
        MypySettingsInitializationTestService.getInstance(project).awaitProcessed(cb)
    }
}