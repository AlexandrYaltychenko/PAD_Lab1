package sender

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.Message
import protocol.Protocol
import protocol.Protocol.CLIENT_INTERVAL
import util.encode
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class ClassicSender constructor(private val clientId : String): Sender {
    private suspend fun makeTask() {
        val uuid = UUID.randomUUID()
        val client = Socket(Protocol.HOST, Protocol.PORT_NUMBER)
        val writer = PrintWriter(client.outputStream)
        val msg = Message(clientType = ClientType.SENDER, clientUid = clientId, payload = UUID.randomUUID().toString())
        println("SENDING $msg")
        writer.println(msg.encode())
        writer.println(uuid)
        writer.flush()
        writer.close()
        client.close()
        delay(1000)
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