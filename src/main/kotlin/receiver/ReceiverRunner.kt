package receiver

import kotlinx.coroutines.experimental.runBlocking
import protocol.Protocol.DEFAULT_QUEUE
import java.util.*

fun main(args: Array<String>) = runBlocking {
    println("Select sender type : 1 - Classic 2 - New")
    val type : Int = readLine()?.toInt() ?: 1
    val receiver : Receiver = when (type) {
        1 -> ClassicReceiver(UUID.randomUUID().toString())
        else -> {
            println("Enter the topic")
            val topic = readLine()
            NewReceiver(UUID.randomUUID().toString(),topic ?: DEFAULT_QUEUE)
        }
    }
    receiver.run()
}