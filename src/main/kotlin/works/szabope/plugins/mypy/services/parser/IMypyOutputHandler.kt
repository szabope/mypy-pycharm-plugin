package works.szabope.plugins.mypy.services.parser

import kotlinx.coroutines.flow.Flow

interface IMypyOutputHandler {
    suspend fun handle(flow: Flow<String>)
    fun getError(): String
}
