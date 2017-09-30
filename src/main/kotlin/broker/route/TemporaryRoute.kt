package broker.route

import broker.queue.ExtendedQueue
import broker.queue.PermanentExtendedQueue
import broker.queue.QueueType
import com.google.gson.reflect.TypeToken
import protocol.Message
import protocol.RoutedMessage
import java.util.*

class TemporaryRoute constructor(override val name: String): AbstractRoute() {
    override val type: QueueType
        get() = QueueType.TEMPORARY
    override val messages: ExtendedQueue<RoutedMessage> = PermanentExtendedQueue(name,true,object : TypeToken<Queue<RoutedMessage>>(){})

    override fun putMessage(msg: Message) {

    }
}