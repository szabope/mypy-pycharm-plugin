package works.szabope.plugins.mypy.services.cli

class CollectingMypyOutputHandler : AbstractMypyOutputHandler() {
    private val results = mutableListOf<MypyOutput>()
    private val errorBuilder = StringBuilder()

    override suspend fun handleResult(result: MypyOutput) {
        results.add(result)
    }

    override suspend fun reportError(error: String) {
        errorBuilder.append(error)
    }

    fun getResults() = results.toList()
    fun getError() = errorBuilder.toString()
}