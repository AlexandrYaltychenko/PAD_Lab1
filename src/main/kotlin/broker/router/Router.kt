package broker.router

import protocol.RoutedMessage
import sun.plugin2.message.Message

interface Router {
    fun put(msg : RoutedMessage)
    fun get(scope : String) : RoutedMessage
    fun cron()
    fun onStop()
}