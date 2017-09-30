package util

import com.google.gson.Gson
import protocol.Message
import protocol.RoutedMessage

fun Message.encode() : String { return Gson().toJson(this) }
fun RoutedMessage.encode() : String { return Gson().toJson(this) }
fun String.asMessage() : Message { return Gson().fromJson(this, Message::class.java) }
fun String.asRoutedMessage() : RoutedMessage {
    val msg =  Gson().fromJson(this, RoutedMessage::class.java)
    return if (msg.topic == null)
        RoutedMessage(msg.clientType,msg.clientUid,msg.payload,"main")
    else
        msg
}