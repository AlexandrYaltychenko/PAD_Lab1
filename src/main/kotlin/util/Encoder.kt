package util

import com.google.gson.Gson
import protocol.Message

fun Message.encode() : String { return Gson().toJson(this) }
fun String.decode() : Message { return Gson().fromJson(this, Message::class.java) }
