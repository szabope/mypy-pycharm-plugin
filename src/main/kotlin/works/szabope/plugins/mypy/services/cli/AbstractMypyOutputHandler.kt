package works.szabope.plugins.mypy.services.cli

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

abstract class AbstractMypyOutputHandler {
    abstract suspend fun handleResult(result: MypyOutput)

    abstract suspend fun reportError(error: String)

    // tricky error handling here:
    // - can't rely on mypy status != 0; https://github.com/python/mypy/issues/6003
    // - stderr is not written by mypy, see `parse`
    // so if parsing output fails (normal output is json), we report the rest of the lines as error lines
    suspend fun handle(flow: Flow<String>) {
        var hasFailed = false
        flow.filter { it.isNotBlank() }.collect {
            try {
                if (!hasFailed) {
                    handleResult(parse(it))
                } else {
                    reportError(it)
                }
            } catch (ignored: SerializationException) {
                hasFailed = true
                reportError(it)
            }
        }
    }

    // mypy prints its own errors to stdout, mixing them into normal output, in which case even `-O json` is ignored
    @Throws(SerializationException::class)
    private fun parse(json: String): MypyOutput {
        val result = Json.decodeFromString(MypyOutput.serializer(), json)
        return result.copy(
            file = result.file,
            line = result.line - 1, // mypy uses 1-based line numbers, IntelliJ uses 0-based ~
            column = result.column,
            message = result.message,
            hint = result.hint,
            code = result.code,
            severity = result.severity.uppercase()
        )
    }
}