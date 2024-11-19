package works.szabope.plugins.mypy.services.cli

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.mypy.messages.MypyScanResultPublisher

class PublishingMypyOutputHandler(private val project: Project) : AbstractMypyOutputHandler() {
    private val errorBuilder = StringBuilder()

    override suspend fun handleResult(result: MypyOutput) = withContext(Dispatchers.EDT) {
        MypyScanResultPublisher(project.messageBus).publish(result)
    }

    override suspend fun reportError(error: String) {
        errorBuilder.appendLine(error)
    }

    fun getError() = errorBuilder.toString()
}