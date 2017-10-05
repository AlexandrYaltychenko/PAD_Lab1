package broker.pool

import protocol.RoutedMessage

interface SubscriberPool {
    suspend fun notify(msg : RoutedMessage)
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
}