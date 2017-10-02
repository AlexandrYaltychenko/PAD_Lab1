package subscriber

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import protocol.ClientType
import protocol.RoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class CustomSubscriber(private val clientId : String, private val scope : String) : ExternalSubscriber {
    private suspend fun processTask() {
        val client = Socket("127.0.0.1", 14141)
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        val writer = PrintWriter(client.outputStream)
        val clientUid = UUID.randomUUID().toString()
        while (true) {
            println("asking for a message...")
            val msg = RoutedMessage(ClientType.SUBSCRIBER, clientUid = clientUid, scope = scope)
            writer.println(msg.encode())
            writer.flush()
            val task = reader.readLine()
            println("PROCESSED " + task)
        }
        client.close()
    }

    override suspend fun run() = runBlocking{
        println("CLIENT $clientId STARTED WORKING...")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("CLIENT $clientId STOPPED WORKING")
        })
        launch(CommonPool) {
            processTask()
        }.join()
    }
}