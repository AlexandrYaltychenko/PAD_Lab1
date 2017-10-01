package broker

import broker.pool.DefaultSubscriber
import broker.route.PermanentRoute
import broker.router.DefaultRouter
import broker.router.Router
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.RoutedMessage
import util.asRoutedMessage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*


class Broker {
    private val timer: Timer = Timer()
    /*private val router : Router = DefaultRouter()

    private fun handleClient(client: Socket) {
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        val writer = PrintWriter(client.outputStream)
        val msg = reader.readLine().asRoutedMessage()
        if (msg.clientType == ClientType.RECEIVER) {
            writer.println(router.get(msg.scope))
            writer.flush()
        } else if (msg.clientType == ClientType.SENDER) {
            router.put(msg)
        }
        writer.close()
        reader.close()
        client.close()
    }*/

    fun testPS(){
        val mainScope = ScopeFactory.fromString("root")
        val root = PermanentRoute(mainScope,"root")
        var msg = RoutedMessage(msg = "Msg1", scope = "google.jora")
        root.putMessage(ScopeFactory.fromString(msg.scope),msg)
        root.putMessage(ScopeFactory.fromString(msg.scope),msg)
        msg = RoutedMessage(msg = "Msg2", scope = "apple.com.imac")
        root.putMessage(ScopeFactory.fromString(msg.scope),msg)
        val subscriber = DefaultSubscriber(ScopeFactory.fromString("root.apple.com"))
        root.subscribe(subscriber)
        println()
        root.print()

    }

    fun runServer() {
        println("Starting server...")
        /*Runtime.getRuntime().addShutdownHook(Thread {
            router.onStop()
        })*/
        val server = ServerSocket(14141)
        testPS()
        /*timer.schedule(object : TimerTask() {
            override fun run() {
                router.cron()
            }
        }, 5000, 5000)
        while (true) {
            val client = server.accept()
            launch(CommonPool) {
                handleClient(client)
            }
        }*/
    }
}