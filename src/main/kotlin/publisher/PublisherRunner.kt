package publisher

import kotlinx.coroutines.experimental.runBlocking
import java.util.*


fun main(args: Array<String>) = runBlocking {
    println("enter topic to publish")
    CustomExternalPublisher(UUID.randomUUID().toString(), readLine()?:"Default").run()
}