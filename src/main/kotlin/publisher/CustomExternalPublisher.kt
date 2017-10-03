package publisher

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.MessageType
import protocol.RoutedMessage
import util.asRoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class CustomExternalPublisher(private val clientId : String, private val scope : String): ExternalPublisher {
    private suspend fun makeTask() {
        val uuid = UUID.randomUUID().toString()
        val client = Socket("127.0.0.1", 14141)
        val writer = PrintWriter(client.outputStream)
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        var msg = RoutedMessage(ClientType.PUBLISHER, clientUid = uuid, scope = scope, messageType = MessageType.CONNECT)
        writer.println(msg)
        writer.flush()
        var response = reader.readLine().asRoutedMessage()
        if (response.messageType == MessageType.ERROR){
            println("Broker rejected the connection: ${response.payload}")
            return
        }
        msg = RoutedMessage(ClientType.PUBLISHER, clientUid = uuid, scope = scope, messageType = MessageType.LAST_WILL)
        writer.println(msg)
        writer.flush()
        response = reader.readLine().asRoutedMessage()
        if (response.messageType == MessageType.ERROR){
            println("Broker rejected to accept the last will: ${response.payload}")
        }
        msg = RoutedMessage(clientType = ClientType.PUBLISHER, clientUid = clientId, payload = UUID.randomUUID().toString(),
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