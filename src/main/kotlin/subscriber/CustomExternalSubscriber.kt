package subscriber

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import protocol.*
import util.asRoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class CustomExternalSubscriber(private val clientId: String, private val scope: String) : ExternalSubscriber {
    private var isConnected: Boolean = true
    private suspend fun processTask() {
        val client = Socket(Protocol.HOST, Protocol.PORT_NUMBER)
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        val writer = PrintWriter(client.outputStream)
        while (true) {
            val msg = RoutedMessage(ClientType.SUBSCRIBER, clientUid = clientId, topic = scope, messageType = MessageType.CONNECT)
            writer.println(msg.encode())
            writer.flush()
            isConnected = true
            val response =
            try {
                reader.readLine().asRoutedMessage()
            } catch (e : Exception){
                null
            }
            println("PROCESSED " + response)
            if (response?.messageType == MessageType.ERROR) {
                isConnected = false
                println("stopping...")
                break
            }
        }
    }

    override suspend fun run() = runBlocking {
        println("CLIENT $clientId STARTED WORKING...")
        Runtime.getRuntime().addShutdownHook(Thread {
            if (isConnected) {
                val connection = Connection(Socket(Protocol.HOST, Protocol.PORT_NUMBER))
                connection.writeMsg(RoutedMessage(clientType = ClientType.SUBSCRIBER, clientUid = clientId, topic = scope, payload = "I decided to disconnect...", messageType = MessageType.DISCONNECT))
                connection.close()
            }
            println("CLIENT $clientId STOPPED WORKING")
        })
        launch(CommonPool) {
            processTask()
        }.join()
    }
}