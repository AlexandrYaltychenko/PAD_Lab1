package broker.route

import broker.Topic
import broker.queue.ExtendedQueue
import broker.queue.QueueType
import broker.queue.TemporaryExtendedQueue
import protocol.RoutedMessage

class TemporaryRoute(override val topic: Topic, override val name: String) : AbstractRoute(topic, name) {
    override val type: QueueType
        get() = QueueType.TEMPORARY
    override val messages: ExtendedQueue<RoutedMessage> = TemporaryExtendedQueue(name)

    override fun makeBackUp(force : Boolean) {
        //doing nothing, because the route is temporary
    }
}