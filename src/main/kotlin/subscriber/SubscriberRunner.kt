package subscriber

import kotlinx.coroutines.experimental.runBlocking
import java.util.*

fun main(args: Array<String>) = runBlocking {
    CustomExternalSubscriber(UUID.randomUUID().toString()).run()
}