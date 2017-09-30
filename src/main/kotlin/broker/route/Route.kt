package broker.route

import protocol.Message

interface Route {
    val name : String
    fun getMessage(scope : String)
    fun putMessage(msg : Message)
}