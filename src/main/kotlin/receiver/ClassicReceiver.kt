package receiver

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.Message
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Math.abs
import java.net.Socket
import java.util.*

class ClassicReceiver constructor(private val clientId: String) : Receiver{
    private suspend fun processTask() {
        val randomizer = Random()
        while (true) {
            val clientUid = UUID.randomUUID().toString()
            println("asking for a message...")
            val client = Socket("127.0.0.1", 14141)
            val reader = BufferedReader(InputStreamReader(client.inputStream))
            val writer = PrintWriter(client.outputStream)
            val msg = Message(ClientType.RECEIVER, clientUid = clientUid)
            writer.println(msg.encode())
            writer.flush()
            val task = reader.readLine()
            println("PROCESSED " + task)
            client.close()
            delay(1000 + (abs(randomizer.nextInt()) % 500).toLong())

        }
    }

    override suspend fun run() {
        println("CLIENT $clientId STARTED WORKING...")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("CLIENT $clientId STOPPED WORKING")
        })
        launch(CommonPool) {
            processTask()
        }.join()
    }

}
