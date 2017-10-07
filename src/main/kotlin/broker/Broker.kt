package broker

import broker.pool.*
import broker.route.PermanentRoute
import broker.route.Route
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.*
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.util.*


class Broker : SubscriberPool {
    private val timer: Timer = Timer()
    private val pingTimer = Timer()
    private val root: Route = PermanentRoute(TopicFactory.fromString("root"), "root")
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
                    publisherPool.addPublisher(DefaultPublisher(msg.clientUid, msg.payload.toLongOrNull() ?: 10000L, topic = msg.topic))
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
                    root.putMessage(TopicFactory.fromString(msg.topic), msg)
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
        root.putMessage(TopicFactory.fromString(msg.topic), msg)
    }

    private suspend fun handleSubscriber(connection: Connection, msg: RoutedMessage) {
        println("handling new subscriber ${msg.clientUid}")
        val subscriber: Subscriber = DefaultSubscriber(this, TopicFactory.fromListString("root", msg.topic), uid = msg.clientUid)
        val invalidTopics: MutableList<Topic> = mutableListOf()
        for (topic in subscriber.topics) {
            val result = root.subscribe(topic, subscriber)
            if (result == TopicRelationship.INCLUDED || result == TopicRelationship.FINAL) {
                subscribers[msg.clientUid] = subscriber
            } else {
                if (result == TopicRelationship.NOT_INCLUDED)
                    root.unsubscribe(topic, subscriber)
                invalidTopics.add(topic)
            }
        }
        if (invalidTopics.size == subscriber.topics.size) {
            println("error! subscription rejected: no routes found!")
            connection.writeMsg(RoutedMessage(clientType = ClientType.SERVER, payload = "No such route! Subscriber not attached!", topic = msg.topic, messageType = MessageType.ERROR))
            connection.close()
            return
        } else if (invalidTopics.size > 0) {
            println("warning! some topics were not found. partial subscription!")
            connection.writeMsg(RoutedMessage(clientType = ClientType.SERVER, payload = "No such routes: ${invalidTopics.joinToString(",") { it.toString() }}", topic = msg.topic, messageType = MessageType.WARNING))
        } else {
            println("successful subscription!")
            connection.writeMsg(RoutedMessage(clientType = ClientType.SERVER, payload = "success!", topic = msg.topic, messageType = MessageType.NORMAL))
        }
        subscriber.handle(connection)
    }

    override fun subscribe(subscriber: Subscriber) {
        for (topic in subscriber.topics)
            root.subscribe(topic, subscriber)
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
            loadTopics(root, json)
        } catch (e: Exception) {
            root.addRoute(PermanentRoute(TopicFactory.fromString("root.Apple")))
            root.addRoute(PermanentRoute(TopicFactory.fromString("root.Google")))
        }
    }

    private fun loadTopics(root: Route, array: JsonArray) {
        if (array.size() == 0)
            return
        for (obj in array) {
            val routeObj = obj.asJsonObject
            val topic = TopicFactory.fromString("${root.topic}.${routeObj.get("name").asString}")
            val newRoute = PermanentRoute(topic)
            root.addRoute(newRoute)
            if (routeObj.has("routes"))
                loadTopics(newRoute, routeObj.getAsJsonArray("routes"))
        }
    }

}