package receiver

import kotlinx.coroutines.experimental.runBlocking
import sender.AlarmSender
import sender.ClassicSender
import sender.Sender
import java.util.*

fun main(args: Array<String>) = runBlocking {
    println("select sender type : 1 - classic 2 - alarm")
    val type : Int = readLine()?.toInt() ?: 1
    val receiver : Receiver = when (type) {
        1 -> ClassicReceiver(UUID.randomUUID().toString())
        else -> AlarmReceiver(UUID.randomUUID().toString())
    }
    receiver.run()
}