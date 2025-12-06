package works.szabope.plugins.mypy.services.parser

import kotlinx.serialization.Serializable
import works.szabope.plugins.common.annotator.ToolMessage

// https://github.com/Kotlin/kotlinx.serialization/issues/2808
@Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
@Serializable
data class MypyMessage(
    val file: String,
    override var line: Int,
    override val column: Int,
    override val message: String,
    val hint: String?,
    val code: String,
    var severity: String
) : ToolMessage
