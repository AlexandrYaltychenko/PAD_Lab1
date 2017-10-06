package util

import com.google.gson.Gson
import protocol.RoutedMessage

fun RoutedMessage.encode() : String { return Gson().toJson(this) }
fun String.asRoutedMessage() : RoutedMessage {
    val msg =  Gson().fromJson(this, RoutedMessage::class.java)
    return if (msg.topic == null)
        RoutedMessage(msg.clientType,msg.clientUid,msg.payload,"main")
    else
        msg
}