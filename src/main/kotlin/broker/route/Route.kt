package broker.route

import broker.Topic
import broker.pool.Subscriber
import broker.queue.QueueType
import protocol.RoutedMessage

interface Route {
    val name : String
    val type : QueueType
    val subscribers : Set<Subscriber>
    val topic: Topic
    val routeCount : Int
    val messageCount : Int
    val lastMessage : Long
    fun getMessages(topic: Topic) : List<RoutedMessage>
    suspend fun putMessage(topic: Topic, msg : RoutedMessage)
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
    fun addRoute(route : Route)
    fun print()
    fun cron()
    fun onStop()
}