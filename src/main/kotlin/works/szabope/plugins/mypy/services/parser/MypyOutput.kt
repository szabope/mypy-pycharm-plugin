package works.szabope.plugins.mypy.services.parser

import kotlinx.serialization.Serializable

// https://github.com/Kotlin/kotlinx.serialization/issues/2808
@Suppress("PROVIDED_RUNTIME_TOO_LOW", "INLINE_CLASSES_NOT_SUPPORTED")
@Serializable
data class MypyOutput(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val hint: String?,
    val code: String,
    val severity: String
)
