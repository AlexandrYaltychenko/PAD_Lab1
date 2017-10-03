package sender

import kotlinx.coroutines.experimental.runBlocking
import protocol.Protocol.DEFAULT_QUEUE
import java.util.*


fun main(args: Array<String>) = runBlocking {
    println("Select sender type : 1 - Classic 2 - New")
    val type: Int = readLine()?.toInt() ?: 1
    val sender: Sender = when (type) {
        1 -> ClassicSender(UUID.randomUUID().toString())
        else -> {
            println("enter the topic")
            val topic = readLine()
            NewSender(UUID.randomUUID().toString(), topic ?: DEFAULT_QUEUE)
        }
    }
    sender.run()
}