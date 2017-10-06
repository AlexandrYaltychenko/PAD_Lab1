package broker.route

import broker.Topic
import broker.queue.ExtendedBackupedQueue
import broker.queue.PermanentExtendedQueue
import broker.queue.QueueType
import com.google.gson.reflect.TypeToken
import protocol.RoutedMessage
import java.util.*

class PermanentRoute(override val topic: Topic, override val name: String) : AbstractRoute(topic, name) {
    override val type: QueueType
        get() = QueueType.PERMANENT
    override val messages: ExtendedBackupedQueue<RoutedMessage> =
            PermanentExtendedQueue(topic.toString().replace('*','-'),
                    true,object : TypeToken<Queue<RoutedMessage>>(){})


    constructor(topic: Topic) : this (topic, topic.last)

    override fun makeBackUp(force : Boolean) {
        messages.backUp(force)
    }
}
