package works.szabope.plugins.mypy.services.parser

class CollectingMypyOutputHandler : AbstractMypyOutputHandler() {
    private val results = mutableListOf<MypyOutput>()

    override suspend fun handleResult(result: MypyOutput) {
        results.add(result)
    }

    fun getResults() = results.toList()
}
