package broker.route

import broker.Scope
import broker.pool.Subscriber
import broker.queue.QueueType
import protocol.Message
import protocol.RoutedMessage

interface Route {
    val name : String
    val type : QueueType
    val subscribers : Set<Subscriber>
    val scope : Scope
    fun getMessages(scope : Scope) : List<RoutedMessage>
    suspend fun putMessage(scope : Scope, msg : RoutedMessage)
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
    fun print()
    fun cron()
}