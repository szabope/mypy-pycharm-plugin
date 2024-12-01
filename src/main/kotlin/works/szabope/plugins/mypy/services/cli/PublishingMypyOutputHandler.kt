package works.szabope.plugins.mypy.services.cli

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.mypy.messages.MypyScanResultPublisher
import java.util.concurrent.atomic.AtomicInteger

class PublishingMypyOutputHandler(private val project: Project) : AbstractMypyOutputHandler() {
    private val _resultCounter = AtomicInteger(0)
    val resultCount
        get() = _resultCounter.get()

    override suspend fun handleResult(result: MypyOutput) {
        withContext(Dispatchers.EDT) {
            MypyScanResultPublisher(project.messageBus).publish(result)
            _resultCounter.incrementAndGet()
        }
    }
}