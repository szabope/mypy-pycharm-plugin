package works.szabope.plugins.mypy.services.cli

class CollectingMypyOutputHandler : AbstractMypyOutputHandler() {
    private val results = mutableListOf<MypyOutput>()

    override suspend fun handleResult(result: MypyOutput) {
        results.add(result)
    }

    fun getResults() = results.toList()
}