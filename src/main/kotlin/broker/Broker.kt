package broker

import broker.router.DefaultRouter
import broker.router.Router
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import util.asRoutedMessage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*


class Broker {
    private val timer: Timer = Timer()
    private val router : Router = DefaultRouter()

    private fun handleClient(client: Socket) {
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        val writer = PrintWriter(client.outputStream)
        val msg = reader.readLine().asRoutedMessage()
        if (msg.clientType == ClientType.RECEIVER) {
            //println("GOT MSG " + msg)
            //println("CONNECTED RECEIVER " + msg.clientUid)
            writer.println(router.get(msg.scope))
            writer.flush()
        } else if (msg.clientType == ClientType.SENDER) {
            //println("CONNECTED SENDER " + msg.clientUid)
            //println("GOT MSG " + msg)
            //println("SCOPE = ${msg.scope}")
            router.put(msg)
        }
        //println("THREAD = " + Thread.currentThread().name)
        writer.close()
        reader.close()
        client.close()
    }

    fun runServer() {
        println("Starting server...")
        Runtime.getRuntime().addShutdownHook(Thread {
            router.onStop()
        })
        val server = ServerSocket(14141)
        timer.schedule(object : TimerTask() {
            override fun run() {
                router.cron()
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