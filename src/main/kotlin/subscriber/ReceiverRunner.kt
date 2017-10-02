package subscriber

import kotlinx.coroutines.experimental.runBlocking
import java.util.*

fun main(args: Array<String>) = runBlocking {
    println("select publisher type : 1 - classic 2 - alarm")
    val type : Int = readLine()?.toInt() ?: 1
    val externalSubscriber: ExternalSubscriber = when (type) {
        1 -> ClassicExternalSubscriber(UUID.randomUUID().toString())
        else -> AlarmExternalSubscriber(UUID.randomUUID().toString())
    }
    externalSubscriber.run()
}