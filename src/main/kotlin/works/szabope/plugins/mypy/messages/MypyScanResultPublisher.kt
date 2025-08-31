package works.szabope.plugins.mypy.messages

import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import works.szabope.plugins.mypy.services.parser.MypyMessage

class MypyScanResultPublisher(messageBus: MessageBus) {

    private val publisher = messageBus.syncPublisher(SCAN_RESULT_TOPIC)

    fun publish(message: MypyMessage) {
        publisher.process(message)
    }

    companion object {
        @JvmStatic
        @ProjectLevel
        val SCAN_RESULT_TOPIC = Topic(IMypyScanResultListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
