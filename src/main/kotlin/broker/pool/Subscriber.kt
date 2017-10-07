package broker.pool

import broker.Topic
import protocol.Connection
import protocol.RoutedMessage

interface Subscriber {
    val uid : String
    val topics: List<Topic>
    suspend fun messagePublished(topic: Topic, message : RoutedMessage)
    suspend fun handle(connection : Connection)
    suspend fun stop()
}