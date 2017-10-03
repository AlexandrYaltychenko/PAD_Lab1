package publisher

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.RoutedMessage
import util.encode
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class CustomPublisher(private val clientId : String, private val scope : String): Publisher {
    private suspend fun makeTask() {
        val uuid = UUID.randomUUID()
        val client = Socket("127.0.0.1", 14141)
        val writer = PrintWriter(client.outputStream)
        val msg = RoutedMessage(clientType = ClientType.PUBLISHER, clientUid = clientId, payload = UUID.randomUUID().toString(),
                scope = scope)
        println("SENDIND $msg")
        writer.println(msg.encode())
        writer.println(uuid)
        writer.flush()
        writer.close()
        client.close()
        delay(1000)
    }

    override suspend fun run() {
        println("SENDER $clientId STARTED WORKING...")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("SENDER $clientId STOPPED WORKING")
        })
        while (true) {
            launch(CommonPool) {
                makeTask()
            }
            delay(1000)
        }
    }
}