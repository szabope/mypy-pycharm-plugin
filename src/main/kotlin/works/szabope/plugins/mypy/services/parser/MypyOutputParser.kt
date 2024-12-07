package works.szabope.plugins.mypy.services.parser

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object MypyOutputParser {

    /**
     * @throws SerializationException mypy _sometimes_ prints its own errors to stdout, mixing them into normal output,
     * in which case even `-O json` is ignored.
      */
    @Throws(SerializationException::class)
    fun parse(json: String): MypyOutput {
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
