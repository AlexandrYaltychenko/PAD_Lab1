package broker

import broker.pool.Subscriber

interface SubscriberPool {
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
}