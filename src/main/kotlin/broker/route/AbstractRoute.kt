package broker.route

import broker.Scope
import broker.ScopeFactory
import broker.ScopeRelationship
import broker.pool.Subscriber
import broker.queue.ExtendedQueue
import protocol.RoutedMessage
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractRoute(override val scope: Scope, override val name: String) : Route {
    protected val routes: MutableMap<String, Route> = ConcurrentHashMap()
    protected abstract val messages: ExtendedQueue<RoutedMessage>
    override val subscribers: MutableSet<Subscriber> = mutableSetOf()
    private val transcribers : MutableSet<Subscriber> = mutableSetOf()

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
        //println("put message")
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
            //println("message put!")
            //println("no next, adding")
            messages.add(msg)
            notifySubscribers()
        }
    }

    private fun newRoute(scope: Scope, name: String): Route {
        val route = TemporaryRoute(scope, name)
        // println("created new route $name with scope = $scope")
        routes[name] = route
        for (subscriber in subscribers.union(transcribers))
            route.subscribe(subscriber)
        return route
    }

    override fun subscribe(subscriber: Subscriber) {
        val relationship = scope.belongsTo(subscriber.scopes)
        //println("subscribe $name to ${publisher.scopes} relationship = $relationship")
        if (relationship == ScopeRelationship.ABORT)
            return
        if (relationship != ScopeRelationship.NOT_INCLUDED)
            subscribers.add(subscriber)
        else
            transcribers.add(subscriber)
        if (relationship != ScopeRelationship.FINAL)
            routes.values.map {
                it.subscribe(subscriber)
            }
    }

    override fun unsubscribe(subscriber: Subscriber) {
        if (scope.belongsTo(subscriber.scopes) == ScopeRelationship.ABORT)
            return
        for (route in routes.values)
            route.unsubscribe(subscriber)
        subscribers.remove(subscriber)
        transcribers.remove(subscriber)
        /*if (!scope.belongsTo(publisher.scopes))
            return
        subscribers.remove(publisher)
        routes.values.map { it.unsubscribe(publisher) }*/
    }

    protected suspend fun notifySubscribers() {
        if (messages.size == 0 || subscribers.size == 0)
            return
        val iterator = messages.iterator()
        while (iterator.hasNext()){
            val msg = iterator.next()
            for (subscriber in subscribers){
                subscriber.messagePublished(scope, msg)
            }
            iterator.remove()
        }
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

    override fun cron() {
        //here should be placed backup job
    }
}