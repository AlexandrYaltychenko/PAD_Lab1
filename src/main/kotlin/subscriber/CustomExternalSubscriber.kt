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

class CustomExternalSubscriber(private val clientId: String) : ExternalSubscriber {
    private var topic: String = "*"
    private var isConnected: Boolean = false
    private suspend fun processTask() {
        val connection = Connection(Socket(Protocol.HOST, Protocol.PORT_NUMBER))
        while (true) {
            val msg = RoutedMessage(ClientType.SUBSCRIBER, clientUid = clientId, topic = topic, messageType = MessageType.CONNECT)
            connection.writeMsg(msg)
            isConnected = true
            val response = connection.readMsg()
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
        val topics = mutableListOf<String>()
        while (true) {
            println("enter topic to subscribe (enter # to finish)")
            val temp = readLine()
            if (temp == null || temp == "#")
                break
            else {
                topics.add(temp)
            }
        }
        topic = topics.joinToString(",") { it }
        println("subscribing to $topic")
        Runtime.getRuntime().addShutdownHook(Thread {
            if (isConnected) {
                val connection = Connection(Socket(Protocol.HOST, Protocol.PORT_NUMBER))
                connection.writeMsg(RoutedMessage(clientType = ClientType.SUBSCRIBER, clientUid = clientId, topic = topic, payload = "I decided to disconnect...", messageType = MessageType.DISCONNECT))
                connection.close()
            }
            println("CLIENT $clientId STOPPED WORKING")
        })
        launch(CommonPool) {
            processTask()
        }.join()
    }
}