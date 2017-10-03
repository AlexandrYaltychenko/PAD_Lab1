package broker

import broker.router.DefaultRouter
import broker.router.Router
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.Protocol
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
        try {
            val msg = reader.readLine().asRoutedMessage()
        if (msg.clientType == ClientType.RECEIVER) {
            writer.println(router.get(msg.topic))
            writer.flush()
        } else if (msg.clientType == ClientType.SENDER) {
            router.put(msg)
        }
        } catch (e: JsonSyntaxException) {
            println("invalid message got... ignoring...")
        } finally {
            writer.close()
            reader.close()
            client.close()
        }
    }

    fun runServer() {
        println("Starting server...")
        Runtime.getRuntime().addShutdownHook(Thread {
            router.onStop()
        })
        val server = ServerSocket(Protocol.PORT_NUMBER)
        timer.schedule(object : TimerTask() {
            override fun run() {
                router.cron()
            }
        }, Protocol.CRON_DELAY, Protocol.CRON_INTERVAL)
        while (true) {
            val client = server.accept()
            launch(CommonPool) {
                handleClient(client)
            }
        }
    }
}