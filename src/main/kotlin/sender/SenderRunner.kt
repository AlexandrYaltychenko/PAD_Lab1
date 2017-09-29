package sender

import kotlinx.coroutines.experimental.runBlocking
import java.util.*


fun main(args: Array<String>) = runBlocking {
    println("select sender type : 1 - classic 2 - alarm")
    val type : Int = readLine()?.toInt() ?: 1
    val sender : Sender = when (type) {
        1 -> ClassicSender(UUID.randomUUID().toString())
        else -> AlarmSender(UUID.randomUUID().toString())
    }
    sender.run()
}