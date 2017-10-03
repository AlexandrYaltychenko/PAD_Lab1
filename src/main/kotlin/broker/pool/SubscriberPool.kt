package broker.pool

interface SubscriberPool {
    fun subscribe(subscriber: Subscriber)
    fun unsubscribe(subscriber: Subscriber)
}