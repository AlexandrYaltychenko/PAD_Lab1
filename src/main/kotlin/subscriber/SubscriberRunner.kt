package subscriber

import kotlinx.coroutines.experimental.runBlocking
import java.util.*

fun main(args: Array<String>) = runBlocking {
    println("enter the scope to subscribe...")
    CustomExternalSubscriber(UUID.randomUUID().toString(), readLine() ?: "*").run()
}