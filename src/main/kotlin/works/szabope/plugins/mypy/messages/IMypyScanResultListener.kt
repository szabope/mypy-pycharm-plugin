package works.szabope.plugins.mypy.messages

import works.szabope.plugins.mypy.services.parser.MypyMessage

fun interface IMypyScanResultListener {
    fun process(result: MypyMessage)
}
