package sender

import kotlinx.coroutines.experimental.runBlocking
import java.util.*


fun main(args: Array<String>) = runBlocking {
    val sender : Sender = ClassicSender(UUID.randomUUID().toString())
    sender.run()
}