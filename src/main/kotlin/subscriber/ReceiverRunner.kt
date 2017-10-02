package subscriber

import broker.ScopeFactory
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

fun main(args: Array<String>) = runBlocking {
    println("enter the scope to subscribe...")
    CustomSubscriber(UUID.randomUUID().toString(), readLine() ?: "*").run()
}