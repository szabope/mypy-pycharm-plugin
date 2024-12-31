package works.szabope.plugins.mypy

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.cancelOnDispose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import works.szabope.plugins.mypy.activity.MypySettingsInitializationActivity
import java.util.concurrent.atomic.AtomicBoolean

@Service
class MypySettingsInitializationTestService(private val project: Project, private val cs: CoroutineScope) {

    private val isStarted = AtomicBoolean(false)
    private val initializationActivity = MypySettingsInitializationActivity()

    suspend fun awaitProcessed(cb: () -> Unit) {
        initializationActivity.configurationCalled.tryReceive() // clear existing
        cb.invoke()
        awaitActivity()
    }

    private suspend fun awaitActivity() {
        initializationActivity.configurationCalled.receive()
    }

    @Suppress("UnstableApiUsage")
    fun executeInitialization() {
        if (isStarted.getAndSet(true)) {
            return
        }
        cs.launch {
            initializationActivity.execute(project)
        }.cancelOnDispose(project, true)
        runBlocking { awaitActivity() }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<MypySettingsInitializationTestService>()
    }
}