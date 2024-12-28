package works.szabope.plugins.mypy

import com.intellij.openapi.components.ComponentManagerEx
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import works.szabope.plugins.mypy.activity.MypySettingsInitializationActivity
import java.util.concurrent.CompletableFuture

abstract class MypyTestCase : BasePlatformTestCase() {

    private val initializationActivity = MypySettingsInitializationActivity()

    override fun setUp() {
        super.setUp()
        executeMypySettingsInitialization()
    }

    // yeah, no guarantee for cb completing f, but in this specific case that doesn't really matter
    suspend fun awaitMypySettingsInitialized(cb: () -> Unit) {
        val f = CompletableFuture<Boolean>()
        initializationActivity.onConfigurationDone { f.complete(true) }
        cb.invoke()
        f.await()
    }

    @Suppress("UnstableApiUsage")
    private fun executeMypySettingsInitialization() {
        (project as ComponentManagerEx).getCoroutineScope().launch {
            initializationActivity.execute(project)
        }
    }
}