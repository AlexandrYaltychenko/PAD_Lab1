package broker.route

import broker.Scope
import broker.pool.Subscriber
import broker.queue.QueueType
import protocol.RoutedMessage

interface Route {
    val name : String
    val type : QueueType
    val subscribers : Set<Subscriber>
    val scope : Scope
    val routeCount : Int
    val messageCount : Int
    val lastMessage : Long
    fun getMessages(scope : Scope) : List<RoutedMessage>
    suspend fun putMessage(scope : Scope, msg : RoutedMessage)
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
    fun addRoute(route : Route)
    fun print()
    fun cron()
    fun onStop()
}