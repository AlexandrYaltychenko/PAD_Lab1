package subscriber

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import protocol.*
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class CustomSubscriber(private val clientId: String, private val scope: String) : ExternalSubscriber {
    private suspend fun processTask() {
        val client = Socket(Protocol.HOST, Protocol.PORT_NUMBER)
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        val writer = PrintWriter(client.outputStream)
        while (true) {
            val msg = RoutedMessage(ClientType.SUBSCRIBER, clientUid = clientId, scope = scope, messageType = MessageType.CONNECT)
            writer.println(msg.encode())
            writer.flush()
            val task = reader.readLine()
            println("PROCESSED " + task)
        }
    }

    override suspend fun run() = runBlocking {
        println("CLIENT $clientId STARTED WORKING...")
        Runtime.getRuntime().addShutdownHook(Thread {
            val connection = Connection(Socket(Protocol.HOST, Protocol.PORT_NUMBER))
            connection.writeMsg(RoutedMessage(clientType = ClientType.SUBSCRIBER, clientUid = clientId, scope = scope, payload = "I decided to disconnect...", messageType = MessageType.DISCONNECT))
            connection.close()
            println("CLIENT $clientId STOPPED WORKING")
        })
        launch(CommonPool) {
            processTask()
        }.join()
    }
}