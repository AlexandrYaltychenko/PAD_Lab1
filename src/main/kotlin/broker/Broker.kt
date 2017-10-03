package broker

import broker.pool.*
import broker.route.PermanentRoute
import broker.route.Route
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.ClientType
import protocol.MessageType
import protocol.RoutedMessage
import util.asRoutedMessage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class Broker : SubscriberPool, PublisherPool {
    private val timer: Timer = Timer()
    private val root: Route = PermanentRoute(ScopeFactory.fromString("root"), "root")
    private val publishers = ConcurrentHashMap<String, Publisher>()

    override fun isPresent(uid: String) =
            publishers.keys.contains(uid)

    override fun addPublisher(publisher: Publisher) {
        if (!isPresent(publisher.uid))
            publishers[publisher.uid] = publisher
    }

    override fun addLastWill(uid: String, lastWill: RoutedMessage) {
        if (!isPresent(uid))
            return
        publishers[uid]?.lastWill = lastWill
    }

    private suspend fun handleClient(client: Socket) {
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        val writer = PrintWriter(client.outputStream)
        val msg = reader.readLine().asRoutedMessage()
        if (msg.clientType == ClientType.PUBLISHER) {
            when (msg.messageType) {
                MessageType.CONNECT -> {
                    addPublisher(DefaultPublisher(msg.clientUid,msg.payload.toLongOrNull() ?: 10000L))
                }
                MessageType.LAST_WILL -> {
                    addLastWill(msg.clientUid,msg)
                }

            }
            root.putMessage(ScopeFactory.fromString(msg.scope), msg)
            writer.close()
            reader.close()
            client.close()
        } else if (msg.clientType == ClientType.SUBSCRIBER) {
            handleSubscriber(reader, writer, client, ScopeFactory.fromString("root.${msg.scope}"))
        }
    }

    private suspend fun handleSubscriber(reader: BufferedReader, writer: PrintWriter, client: Socket, scope: Scope) {
        val subscriber: Subscriber = DefaultSubscriber(this, scope)
        root.subscribe(subscriber)
        subscriber.handle(client, reader, writer)
    }

    suspend fun testPS() {
        val mainScope = ScopeFactory.fromString("root")
        val root = PermanentRoute(mainScope, "root")
        var msg = RoutedMessage(payload = "Msg1", scope = "google.jora")
        root.putMessage(ScopeFactory.fromString(msg.scope), msg)
        root.putMessage(ScopeFactory.fromString(msg.scope), msg)
        msg = RoutedMessage(payload = "Msg2", scope = "apple.com.imac")
        root.putMessage(ScopeFactory.fromString(msg.scope), msg)
        val subscriber = DefaultSubscriber(this, ScopeFactory.fromString("root.apple.*"))
        root.subscribe(subscriber)
        println()
        root.print()
        println()
        println()
        msg = RoutedMessage(payload = "Msg3", scope = "apple.com.imac.ssd.512")
        root.putMessage(ScopeFactory.fromString(msg.scope), msg)
        println()
        root.print()
    }

    override fun subscribe(subscriber: Subscriber) {
        root.subscribe(subscriber)
    }

    override fun unsubscribe(subscriber: Subscriber) {
        println("unsubscribing...")
        root.unsubscribe(subscriber)
    }

    suspend fun runServer() {
        println("Starting server...")
        preparePermanentRoutes()
        Runtime.getRuntime().addShutdownHook(Thread {
            root.onStop()
        })
        val server = ServerSocket(14141)
        timer.schedule(object : TimerTask() {
            override fun run() {
                root.cron()
                println()
                root.print()
                println()
            }
        }, 5000, 5000)
        while (true) {
            val client = server.accept()
            launch(CommonPool) {
                handleClient(client)
            }
        }
    }

    private fun preparePermanentRoutes() {
        root.addRoute(PermanentRoute(ScopeFactory.fromString("root.Apple")))
        root.addRoute(PermanentRoute(ScopeFactory.fromString("root.Google")))
    }
}