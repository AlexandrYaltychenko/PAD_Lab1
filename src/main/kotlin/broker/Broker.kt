package broker

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import protocol.ClientType
import protocol.Message
import util.decode
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue

class Broker {
    private val queue = LinkedBlockingQueue<Message>()

    private fun backupQueue() {
        println("BACKUP COMPLETED "+queue.encode())
    }


    private fun handleClient(client: Socket) {
        println("handling client...")
        backupQueue()
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        val writer = PrintWriter(client.outputStream)
        val msg = reader.readLine().decode()
        if (msg.clientType == ClientType.RECEIVER) {
            println("CONNECTED RECEIVER " + msg.clientUid)
            if (queue.isEmpty())
                writer.println(Message(clientType = ClientType.SERVER, msg = "IDLE"))
            else
                writer.println(queue.poll())
            writer.flush()
        } else if (msg.clientType == ClientType.SENDER) {
            println("CONNECTED SENDER " + msg.clientUid)
            //println("GOT MSG " + msg.msg)
            queue.add(msg)
        }
        println("THREAD = " + Thread.currentThread().name)
        writer.close()
        reader.close()
        client.close()
    }

    fun runServer() {
        println("Starting server...")
        val server = ServerSocket(14141)
        while (true) {
            println("Waiting for a client...")
            val client = server.accept()
            launch(CommonPool) {
                handleClient(client)
            }
        }
    }
}