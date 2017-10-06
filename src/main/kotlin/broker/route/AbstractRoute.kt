package broker.route

import broker.Topic
import broker.TopicFactory
import broker.TopicRelationship
import broker.pool.Subscriber
import broker.queue.ExtendedQueue
import broker.queue.QueueType
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.RoutedMessage
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractRoute(override val topic: Topic, override val name: String) : Route {
    protected val routes: MutableMap<String, Route> = ConcurrentHashMap()
    protected abstract val messages: ExtendedQueue<RoutedMessage>
    override val subscribers: MutableSet<Subscriber> = mutableSetOf()
    private val transcribers: MutableSet<Subscriber> = mutableSetOf()
    override val routeCount: Int
        get() = routes.size
    override var lastMessage: Long = System.currentTimeMillis()
    override val messageCount: Int
        get() = messages.size

    override fun getMessages(topic: Topic): List<RoutedMessage> {
        val msgList = mutableListOf<RoutedMessage>()
        if (topic.hasNext()) {
            val targetRoutes = mutableListOf<Route>()
            val target = topic.peek()
            target?.apply {
                if (target == "*") {
                    targetRoutes.addAll(routes.values)
                    topic.next()
                } else {
                    val route = routes[target]
                    if (route != null)
                        targetRoutes.add(route)
                }
            }
            for (route in targetRoutes)
                msgList.addAll(route.getMessages(topic))
        } else {
            msgList.addAll(messages)
        }
        return msgList
    }

    override suspend fun putMessage(topic: Topic, msg: RoutedMessage) {
        if (topic.hasNext()) {
            val target = topic.peek()
            target?.apply {
                topic.next()
                val route = routes[target]
                if (route != null) {
                    route.putMessage(topic, msg)
                } else {
                    newRoute(TopicFactory.appendToEnd(this@AbstractRoute.topic, target), target).putMessage(topic, msg)
                }
            }
        } else {
            lastMessage = System.currentTimeMillis()
            messages.add(msg)
            notifySubscribers()
        }
    }

    private fun newRoute(topic: Topic, name: String): Route {
        val route = TemporaryRoute(topic, name)
        routes[name] = route
        for (subscriber in subscribers.union(transcribers))
            route.subscribe(subscriber)
        return route
    }

    override fun subscribe(subscriber: Subscriber) {
        val relationship = topic.belongsTo(subscriber.topics)
        if (relationship == TopicRelationship.ABORT)
            return
        if (relationship != TopicRelationship.NOT_INCLUDED) {
            subscriber.isAttached = true
            subscribers.add(subscriber)
            launch(CommonPool) {
                notifySubscribers()
            }
        } else
            transcribers.add(subscriber)
        if (relationship != TopicRelationship.FINAL)
            routes.values.map {
                it.subscribe(subscriber)
            }
    }

    override fun unsubscribe(subscriber: Subscriber) {
        if (topic.belongsTo(subscriber.topics) == TopicRelationship.ABORT) {
            return
        }
        for (route in routes.values)
            route.unsubscribe(subscriber)
        subscribers.remove(subscriber)
        transcribers.remove(subscriber)
    }

    @Synchronized
    protected suspend fun notifySubscribers() {
        if (messages.size == 0 || subscribers.size == 0)
            return
        while (messages.isNotEmpty()) {
            val msg = messages.remove()
            for (subscriber in subscribers) {
                subscriber.messagePublished(topic, msg)
            }
        }
    }

    override fun toString(): String {
        return "Route $name with topic = $topic with routes = ${routes.keys} and ${messages.size} messages and ${subscribers.size} subscribers and ${transcribers.size} transcribers"
    }

    override fun print() {
        println()
        repeat(topic.levelsCount, { print("-") })
        print(toString())
        for (route in routes.values)
            route.print()
    }

    override fun cron() {
        val iterator = routes.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            item.value.cron()
            if (item.value.type == QueueType.TEMPORARY && item.value.routeCount == 0 && item.value.messageCount == 0 && item.value.lastMessage < System.currentTimeMillis() - 5000)
                iterator.remove()
        }
        makeBackUp(false)

    }

    override fun addRoute(route: Route) {
        if (routes[route.name] == null)
            routes[route.name] = route
    }

    protected abstract fun makeBackUp(force : Boolean)

    @Synchronized
    override fun onStop() {
        routes.values.map {
            it.onStop()
        }
        if (type == QueueType.PERMANENT)
            makeBackUp(true)
    }
}