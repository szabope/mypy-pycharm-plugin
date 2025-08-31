package works.szabope.plugins.mypy.services.parser

import works.szabope.plugins.common.messages.MessageConverter
import works.szabope.plugins.common.toolWindow.TreeModelDataItem
import works.szabope.plugins.mypy.services.mypySeverityConfigs
import works.szabope.plugins.mypy.toolWindow.MypyTreeModelDataItem

object MypyMessageConverter : MessageConverter<MypyMessage, TreeModelDataItem> {
    override fun convert(message: MypyMessage): TreeModelDataItem {
        val severity = requireNotNull(mypySeverityConfigs[message.severity]) {
            """Mypy message with type '${message.severity}' is not supported. Please, report this issue at  
                    |https://github.com/szabope/mypy-pycharm-plugin/issues""".trimMargin()
        }
        return with(message) {
            MypyTreeModelDataItem(file, line, column, this.message, code, severity, hint)
        }
    }
}