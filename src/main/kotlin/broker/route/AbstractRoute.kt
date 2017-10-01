package broker.route

import broker.Scope
import broker.ScopeFactory
import broker.ScopeRelationship
import broker.pool.Subscriber
import broker.queue.ExtendedQueue
import protocol.Message
import protocol.RoutedMessage
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractRoute(override val scope: Scope, override val name: String) : Route {
    protected val routes: MutableMap<String, Route> = ConcurrentHashMap()
    protected abstract val messages: ExtendedQueue<RoutedMessage>
    override val subscribers: MutableSet<Subscriber> = mutableSetOf()

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

    override fun putMessage(scope: Scope, msg: RoutedMessage) {
        /*println()
        println("route : $name (${this.scope})\nputting to $scope")*/
        if (scope.hasNext()) {
            //println("has next")
            val target = scope.peek()
            target?.apply {
                scope.next()
                val route = routes[target]
                if (route != null) {
                    route.putMessage(scope, msg)
                } else {
                    //println("should create new route")
                    newRoute(ScopeFactory.appendToEnd(this@AbstractRoute.scope, target), target).putMessage(scope, msg)
                }
            }
        } else {
            //println("no next, adding")
            messages.add(msg)
        }
    }

    private fun newRoute(scope: Scope, name: String): Route {
        val route = TemporaryRoute(scope, name)
        // println("created new route $name with scope = $scope")
        routes[name] = route
        return route
    }

    override fun subscribe(subscriber: Subscriber) {
        val relationship = scope.belongsTo(subscriber.scopes)
        println("subscribe $name to ${subscriber.scopes} relationship = $relationship")
        if (relationship == ScopeRelationship.ABORT)
            return
        if (relationship != ScopeRelationship.NOT_INCLUDED)
            subscribers.add(subscriber)
        if (relationship != ScopeRelationship.FINAL)
            routes.values.map {
                it.subscribe(subscriber)
            }
    }

    override fun unsubscribe(subscriber: Subscriber) {
        /*if (!scope.belongsTo(subscriber.scopes))
            return
        subscribers.remove(subscriber)
        routes.values.map { it.unsubscribe(subscriber) }*/
    }

    protected fun publish() {
        if (messages.size == 0 || subscribers.size == 0)
            return
        for (msg in messages)
            for (subscriber in subscribers)
                subscriber.messagePublished(scope, msg)
    }

    override fun toString(): String {
        return "Route $name with scope = $scope with routes = ${routes.keys} and ${messages.size} messages and ${subscribers.size} subscribers"
    }

    override fun print() {
        println()
        repeat(scope.levelsCount, { print("-") })
        print(toString())
        for (route in routes.values)
            route.print()
    }
}