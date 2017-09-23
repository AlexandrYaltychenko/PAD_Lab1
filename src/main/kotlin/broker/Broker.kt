package broker

import broker.queue.DefaultExtendedQueue
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.Message
import util.decode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*


class Broker {
    private val timer: Timer = Timer()
    private val queue = DefaultExtendedQueue<Message>("main")

    private fun handleClient(client: Socket) {
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
        timer.schedule(object : TimerTask() {
            override fun run() {
                println("trying to backup ${queue.name} (${queue.size} items)... in "+Thread.currentThread().name)
                queue.backUp(false)
            }
        }, 5000, 5000)
        while (true) {
            val client = server.accept()
            launch(CommonPool) {
                handleClient(client)
            }
        }
    }
}