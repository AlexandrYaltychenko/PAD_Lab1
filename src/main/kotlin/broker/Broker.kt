package broker

import broker.pool.*
import broker.route.PermanentRoute
import broker.route.Route
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.*
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.*


class Broker : SubscriberPool {
    private val timer: Timer = Timer()
    private val pingTimer = Timer()
    private val root: Route = PermanentRoute(ScopeFactory.fromString("root"), "root")
    private val publisherPool = DefaultPublisherPool(this)
    private val subscribers = mutableMapOf<String, Subscriber>()


    private suspend fun handleClient(client: Socket) {
        val connection = Connection(client)
        val msg = connection.readMsg()
        if (msg == null) {
            println("got invalid message... rejecting...")
            connection.close()
            return
        }
        if (msg.clientType == ClientType.PUBLISHER) {
            when (msg.messageType) {
                MessageType.CONNECT -> {
                    println("New publisher connected")
                    publisherPool.addPublisher(DefaultPublisher(msg.clientUid, msg.payload.toLongOrNull() ?: 10000L, scope = msg.scope))
                    connection.writeMsg(RoutedMessage(ClientType.SERVER, payload = "client connected", messageType = MessageType.NORMAL))
                }
                MessageType.LAST_WILL -> {
                    println("publisher ${msg.clientUid} sent its last will")
                    publisherPool.addLastWill(msg.clientUid, msg)
                    connection.writeMsg(RoutedMessage(ClientType.SERVER, payload = "last will saved", messageType = MessageType.NORMAL))
                }
                MessageType.DISCONNECT -> {
                    println("publisher ${msg.clientUid} performed a planned disconnection")
                    publisherPool.disconnectPublisher(msg.clientUid)
                }
                else -> {
                    publisherPool.confirmPublisher(msg.clientUid)
                    root.putMessage(ScopeFactory.fromString(msg.scope), msg)
                }

            }
        } else if (msg.clientType == ClientType.SUBSCRIBER) {
            when (msg.messageType) {
                MessageType.DISCONNECT -> {
                    println("disconnecting subscriber")
                    val subscriber = subscribers[msg.clientUid]
                    if (subscriber == null)
                        println("not found! ${msg.clientUid}")
                    subscriber?.let {
                        println("unsubscribing ${msg.clientUid}")
                        subscriber.stop()
                        unsubscribe(subscriber)
                    }
                }
                else -> handleSubscriber(connection, msg)
            }
        }
        connection.close()
    }

    override suspend fun notify(msg: RoutedMessage) {
        root.putMessage(ScopeFactory.fromString(msg.scope), msg)
    }

    private suspend fun handleSubscriber(connection: Connection, msg: RoutedMessage) {
        println("handling new subscriber ${msg.clientUid}")
        val subscriber: Subscriber = DefaultSubscriber(this, ScopeFactory.fromString("root.${msg.scope}"), uid = msg.clientUid)
        root.subscribe(subscriber)
        if (subscriber.isAttached){
            println("Subscription accepted!")
            subscribers[msg.clientUid] = subscriber
            subscriber.handle(connection)
        }
        else {
            println("Error! No routes found...")
            unsubscribe(subscriber)
            connection.writeMsg(RoutedMessage(clientType = ClientType.SERVER,payload = "No such route",scope = msg.scope, messageType = MessageType.ERROR))
            connection.close()
            println("error sent!")
        }
    }

    override fun subscribe(subscriber: Subscriber) {
        root.subscribe(subscriber)
    }

    override fun unsubscribe(subscriber: Subscriber) {
        root.unsubscribe(subscriber)
    }

    suspend fun runServer() {
        println("Starting server...")
        preparePermanentRoutes()
        Runtime.getRuntime().addShutdownHook(Thread {
            root.onStop()
        })
        val server = ServerSocket(Protocol.PORT_NUMBER)
        timer.schedule(object : TimerTask() {
            override fun run() {
                root.cron()
                println()
                root.print()
                println()
                println()
            }
        }, Protocol.CRON_DELAY, Protocol.CRON_INTERVAL)
        pingTimer.schedule(object : TimerTask() {
            override fun run() {
                launch(CommonPool) {
                    publisherPool.cron()
                }
            }
        }, Protocol.CRON_DELAY, Protocol.CRON_INTERVAL)
        while (true) {
            val client = server.accept()
            launch(CommonPool) {
                handleClient(client)
            }
        }
    }

    private fun preparePermanentRoutes() {
        try {
            val json = JsonParser().parse(File("permanent.json").bufferedReader().use { it.readText() }).asJsonArray
            loadScopes(root, json)
        } catch (e: Exception) {
            root.addRoute(PermanentRoute(ScopeFactory.fromString("root.Apple")))
            root.addRoute(PermanentRoute(ScopeFactory.fromString("root.Google")))
        }
    }

    private fun loadScopes(root: Route, array: JsonArray) {
        if (array.size() == 0)
            return
        for (obj in array) {
            val routeObj = obj.asJsonObject
            val scope = ScopeFactory.fromString("${root.scope}.${routeObj.get("name").asString}")
            val newRoute = PermanentRoute(scope)
            root.addRoute(newRoute)
            if (routeObj.has("routes"))
                loadScopes(newRoute, routeObj.getAsJsonArray("routes"))
        }
    }

}