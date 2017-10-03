package broker.pool

import protocol.RoutedMessage

interface PublisherPool {
    fun isPresent(uid : String) : Boolean
    fun addPublisher(publisher : Publisher)
    fun addLastWill(uid : String, lastWill : RoutedMessage)
    fun confirmPublisher(uid : String)
    fun cron()
}