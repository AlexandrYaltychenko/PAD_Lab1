package broker.route

import broker.Scope
import broker.ScopeFactory
import broker.ScopeRelationship
import broker.pool.Subscriber
import broker.queue.ExtendedQueue
import broker.queue.QueueType
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import protocol.RoutedMessage
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractRoute(override val scope: Scope, override val name: String) : Route {
    protected val routes: MutableMap<String, Route> = ConcurrentHashMap()
    protected abstract val messages: ExtendedQueue<RoutedMessage>
    override val subscribers: MutableSet<Subscriber> = mutableSetOf()
    private val transcribers: MutableSet<Subscriber> = mutableSetOf()
    override val routeCount: Int
        get() = routes.size
    override var lastMessage: Long = System.currentTimeMillis()
    override val messageCount: Int
        get() = messages.size

    override fun getMessages(scope: Scope): List<RoutedMessage> {
        val msgList = mutableListOf<RoutedMessage>()
        if (scope.hasNext()) {
            val targetRoutes = mutableListOf<Route>()
            val target = scope.peek()
            target?.apply {
                if (target == "*") {
                    targetRoutes.addAll(routes.values)
                    scope.next()
                } else {
                    val route = routes[target]
                    if (route != null)
                        targetRoutes.add(route)
                }
            }
            for (route in targetRoutes)
                msgList.addAll(route.getMessages(scope))
        } else {
            msgList.addAll(messages)
        }
        return msgList
    }

    override suspend fun putMessage(scope: Scope, msg: RoutedMessage) {
        if (scope.hasNext()) {
            val target = scope.peek()
            target?.apply {
                scope.next()
                val route = routes[target]
                if (route != null) {
                    route.putMessage(scope, msg)
                } else {
                    newRoute(ScopeFactory.appendToEnd(this@AbstractRoute.scope, target), target).putMessage(scope, msg)
                }
            }
        } else {
            lastMessage = System.currentTimeMillis()
            messages.add(msg)
            notifySubscribers()
        }
    }

    private fun newRoute(scope: Scope, name: String): Route {
        val route = TemporaryRoute(scope, name)
        routes[name] = route
        for (subscriber in subscribers.union(transcribers))
            route.subscribe(subscriber)
        return route
    }

    override fun subscribe(subscriber: Subscriber) {
        val relationship = scope.belongsTo(subscriber.scopes)
        if (relationship == ScopeRelationship.ABORT)
            return
        if (relationship != ScopeRelationship.NOT_INCLUDED) {
            subscribers.add(subscriber)
            launch(CommonPool) {
                notifySubscribers()
            }
        } else
            transcribers.add(subscriber)
        if (relationship != ScopeRelationship.FINAL)
            routes.values.map {
                it.subscribe(subscriber)
            }
    }

    override fun unsubscribe(subscriber: Subscriber) {
        if (scope.belongsTo(subscriber.scopes) == ScopeRelationship.ABORT) {
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
                subscriber.messagePublished(scope, msg)
            }
        }
    }

    override fun toString(): String {
        return "Route $name with scope = $scope with routes = ${routes.keys} and ${messages.size} messages and ${subscribers.size} subscribers and ${transcribers.size} transcribers"
    }

    override fun print() {
        println()
        repeat(scope.levelsCount, { print("-") })
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