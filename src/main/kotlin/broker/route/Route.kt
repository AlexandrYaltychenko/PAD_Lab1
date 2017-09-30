package broker.route

import broker.Scope
import broker.queue.QueueType
import protocol.Message
import protocol.RoutedMessage

interface Route {
    val name : String
    val type : QueueType
    fun getMessages(scope : Scope) : List<RoutedMessage>
    fun putMessage(msg : Message)
}