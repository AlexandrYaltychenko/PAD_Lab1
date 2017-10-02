package publisher

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.Message
import util.encode
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class ClassicPublisher constructor(private val clientId : String): Publisher {
    private suspend fun makeTask() {
        println("making task...")
        val uuid = UUID.randomUUID()
        val client = Socket("127.0.0.1", 14141)
        val writer = PrintWriter(client.outputStream)
        val msg = Message(clientType = ClientType.PUBLISHER, clientUid = clientId,msg = UUID.randomUUID().toString())
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
            delay(1000)
        }
    }
}