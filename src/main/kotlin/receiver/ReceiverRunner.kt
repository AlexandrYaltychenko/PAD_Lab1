package receiver

import kotlinx.coroutines.experimental.runBlocking
import java.util.*

fun main(args: Array<String>) = runBlocking {
    val receiver : Receiver = ClassicReceiver(UUID.randomUUID().toString())
    receiver.run()
}