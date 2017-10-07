package publisher

import kotlinx.coroutines.experimental.runBlocking
import protocol.Protocol
import java.util.*


fun main(args: Array<String>) = runBlocking {
    println("enter topic to publish")
    val topic = readLine()?:"*"
    println("enter interval to publish")
    var interval = readLine()?.toLongOrNull()?: Protocol.CLIENT_INTERVAL
    if (interval < 1000 )
        interval = 1000
    else
        if (interval > 60000)
            interval = 60000
    CustomExternalPublisher(UUID.randomUUID().toString(), interval, topic).run()
}