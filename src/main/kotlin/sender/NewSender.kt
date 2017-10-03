package sender

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.Protocol
import protocol.Protocol.CLIENT_INTERVAL
import protocol.RoutedMessage
import util.encode
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class NewSender(private val clientId : String, private val topic : String): Sender {
    private suspend fun makeTask() {
        val uuid = UUID.randomUUID()
        val client = Socket(Protocol.HOST, Protocol.PORT_NUMBER)
        val writer = PrintWriter(client.outputStream)
        val msg = RoutedMessage(clientType = ClientType.SENDER,
                clientUid = clientId,
                payload = UUID.randomUUID().toString(),
                topic = topic)
        println("sending $msg")
        writer.println(msg.encode())
        writer.println(uuid)
        writer.flush()
        writer.close()
        client.close()
        delay(CLIENT_INTERVAL)
    }

    override suspend fun run() {
        val clientId = UUID.randomUUID()
        println("SENDER $clientId STARTED WORKING...")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("SENDER $clientId STOPPED WORKING")
        })
        while (true) {
            launch(CommonPool) {
                makeTask()
            }
            delay(CLIENT_INTERVAL)
        }
    }
}