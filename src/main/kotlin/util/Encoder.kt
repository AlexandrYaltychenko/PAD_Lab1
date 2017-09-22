package util

import com.google.gson.Gson
import kotlinx.coroutines.experimental.channels.Channel
import protocol.Message
import java.util.*

fun Message.encode() = Gson().toJson(this)
fun String.decode() = Gson().fromJson(this, Message::class.java)
fun Queue<Message>.encode() = Gson().toJson(this)