package works.szabope.plugins.mypy.messages

import works.szabope.plugins.mypy.services.parser.MypyOutput

fun interface IMypyScanResultListener {
    fun process(result: MypyOutput)
}
