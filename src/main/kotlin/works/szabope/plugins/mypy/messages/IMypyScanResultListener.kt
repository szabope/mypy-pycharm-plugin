package works.szabope.plugins.mypy.messages

import works.szabope.plugins.mypy.services.cli.MypyOutput

fun interface IMypyScanResultListener {
    fun process(result: MypyOutput)
}