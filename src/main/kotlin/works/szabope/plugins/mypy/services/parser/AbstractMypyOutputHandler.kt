package works.szabope.plugins.mypy.services.parser

import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.SerializationException

abstract class AbstractMypyOutputHandler : IMypyOutputHandler {
    private val logger = logger<AbstractMypyOutputHandler>()
    private val errorBuilder = StringBuilder()

    abstract suspend fun handleResult(result: MypyOutput)

    // tricky error handling here (we expect json output):
    // - can't rely on mypy status != 0; https://github.com/python/mypy/issues/6003
    // - stderr is not _always_ written by mypy, see `parse`
    // - sometimes errors and warnings are mixed into json output
    // - and sometimes after a warning comes valuable json output
    // so if parsing output fails, we consider it an as error from mypy, but keep going
    override suspend fun handle(flow: Flow<String>) {
        flow.filter { it.isNotBlank() }.collect {
            try {
                val result = MypyOutputParser.parse(it)
                handleResult(result)
            } catch (e: SerializationException) {
                logger.debug(e)
                reportError(it)
            }
        }
    }

    override fun getError() = errorBuilder.toString()

    private fun reportError(error: String) {
        errorBuilder.appendLine(error)
    }
}
