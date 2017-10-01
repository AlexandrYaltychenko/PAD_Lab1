package broker.pool

import protocol.RoutedMessage

interface Pool {
    fun publish(message : RoutedMessage)
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
}