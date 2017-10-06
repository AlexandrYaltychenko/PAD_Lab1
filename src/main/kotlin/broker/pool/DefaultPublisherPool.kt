package broker.pool

import protocol.ClientType
import protocol.MessageType
import protocol.RoutedMessage
import java.util.concurrent.ConcurrentHashMap

class DefaultPublisherPool(private val subscriberPool: SubscriberPool) : PublisherPool {

    private val publishers = ConcurrentHashMap<String, Publisher>()
    override fun isPresent(uid: String) =
            publishers.keys.contains(uid)

    override fun addPublisher(publisher: Publisher) {
        if (!isPresent(publisher.uid))
            publishers[publisher.uid] = publisher
    }

    override fun addLastWill(uid: String, lastWill: RoutedMessage) {
        if (!isPresent(uid))
            return
        publishers[uid]?.lastWill = lastWill
    }

    override fun confirmPublisher(uid: String) {
        publishers[uid]?.confirm()
    }

    override fun disconnectPublisher(uid: String) {
        publishers.remove(uid)
    }

    @Synchronized
    override suspend fun cron() {
        val iterator = publishers.iterator()
        while (iterator.hasNext()) {
            val publisher = iterator.next().value
            if (publisher.isDead) {
                val notification = "Warning! Publisher ${publisher.uid} is Dead!!! Sending notification..."
                println("\n$notification")
                val lastWill = publisher.lastWill ?: RoutedMessage(ClientType.SERVER, payload = notification, topic = publisher.topic.toString(), messageType = MessageType.ERROR)
                subscriberPool.notify(lastWill)
                iterator.remove()
            }
        }
    }
}