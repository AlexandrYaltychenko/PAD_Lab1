package broker.pool

import broker.Topic
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import protocol.Connection
import protocol.RoutedMessage

class DefaultSubscriber(private val subscriberPool: SubscriberPool, topics : List<Topic>, override val uid: String) : Subscriber {
    override val topics: MutableList<Topic> = topics.toMutableList()
    private val channel: Channel<RoutedMessage> = Channel()
    override var isAttached: Boolean = false
    private var connection : Connection? = null

    override suspend fun messagePublished(topic: Topic, message: RoutedMessage) {
        channel.send(message)
    }

    override suspend fun handle(connection: Connection) {
        this.connection = connection
        while (true) {
            val msg = channel.receive()
            if (connection.isClosed){
                break
            }
            connection.writeMsg(msg)
            if (connection.writer.checkError()) {
                break
            }
            delay(250)
        }
        connection.close()
        subscriberPool.unsubscribe(this)
    }

    suspend override fun stop() {
        while (!channel.isEmpty) {
            channel.poll()
        }
    }
}