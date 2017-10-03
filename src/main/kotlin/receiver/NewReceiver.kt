package receiver

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import protocol.ClientType
import protocol.Protocol
import protocol.RoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class NewReceiver(private val clientId : String, private val topic : String) : Receiver{
    private suspend fun processTask() {
        val randomizer = Random()
        while (true) {
            val clientUid = UUID.randomUUID().toString()
            val client = Socket(Protocol.HOST, Protocol.PORT_NUMBER)
            val reader = BufferedReader(InputStreamReader(client.inputStream))
            val writer = PrintWriter(client.outputStream)
            val msg = RoutedMessage(ClientType.RECEIVER, clientUid = clientUid, topic = topic)
            writer.println(msg.encode())
            writer.flush()
            val task = reader.readLine()
            println("PROCESSED " + task)
            client.close()
            delay(Protocol.CLIENT_INTERVAL + (Math.abs(randomizer.nextInt()) % 500).toLong())

        }
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