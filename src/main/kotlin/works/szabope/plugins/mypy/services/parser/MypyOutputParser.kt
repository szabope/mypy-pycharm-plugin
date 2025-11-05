package works.szabope.plugins.mypy.services.parser

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Created when a line of mypy output cannot be parsed for some reason.
 * The goal is to handle cases when mypy fails to distinguish between throwing an exception and reporting a hit.
 *  - mypy exceptions _sometimes_ printed to stdout, mixing them into normal output, in which case even `-O json` is ignored
 *  - and sometimes after such and exception comes valuable json output
 */
class MypyParseException(val sourceJson: String, override val cause: SerializationException) :
    SerializationException(cause.message, cause)

object MypyOutputParser {

    private val withUnknownKeys = Json { ignoreUnknownKeys = true }

    /**
     * @throws SerializationException mypy _sometimes_ prints its own errors to stdout, mixing them into normal output,
     * in which case even `-O json` is ignored.
     */
    @Throws(SerializationException::class)
    fun parse(json: String): Result<MypyMessage> {
        val result = try {
            withUnknownKeys.decodeFromString(MypyMessage.serializer(), json)
        } catch (e: SerializationException) {
            return Result.failure(MypyParseException(json, e))
        }
        return Result.success(adjustForPlatform(result))
    }

    /**
     * Adjust line numbers
     *   from mypy: 1-based
     *   to intellij: 0-based
     */
    private fun adjustForPlatform(message: MypyMessage): MypyMessage {
        return message.copy(
            file = message.file,
            line = message.line - 1,
            column = message.column,
            message = message.message,
            hint = message.hint,
            code = message.code,
            severity = message.severity.uppercase()
        )
    }
}
