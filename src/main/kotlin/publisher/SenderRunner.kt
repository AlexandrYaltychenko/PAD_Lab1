package publisher

import kotlinx.coroutines.experimental.runBlocking
import java.util.*


fun main(args: Array<String>) = runBlocking {
    println("select publisher type : 1 - classic 2 - alarm")
    val type : Int = readLine()?.toInt() ?: 1
    val publisher: Publisher = when (type) {
        1 -> ClassicPublisher(UUID.randomUUID().toString())
        else -> AlarmPublisher(UUID.randomUUID().toString())
    }
    publisher.run()
}