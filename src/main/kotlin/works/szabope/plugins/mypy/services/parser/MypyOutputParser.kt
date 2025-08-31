package works.szabope.plugins.mypy.services.parser

import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class MypyParseException(val sourceJson: String, override val cause: SerializationException) :
    SerializationException(cause.message, cause)

object MypyOutputParser {

    private val withUnknownKeys = Json { ignoreUnknownKeys = true }

    suspend fun parse(stdout: Flow<String>): Flow<Result<MypyMessage>> {
        return stdout.buffer(capacity = UNLIMITED).transform { emit(doParse(it)) }
    }

    /**
     * @throws SerializationException mypy _sometimes_ prints its own errors to stdout, mixing them into normal output,
     * in which case even `-O json` is ignored.
     */
    @Throws(SerializationException::class)
    private fun doParse(json: String): Result<MypyMessage> {
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
