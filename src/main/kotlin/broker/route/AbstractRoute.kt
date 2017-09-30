package broker.route

import broker.Scope
import broker.queue.ExtendedQueue
import protocol.RoutedMessage
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractRoute : Route {
    protected val routes: Map<String, Route> = ConcurrentHashMap()
    protected abstract val messages : ExtendedQueue<RoutedMessage>

    override fun getMessages(scope: Scope) : List<RoutedMessage>{
        val msgList = mutableListOf<RoutedMessage>()
        if (scope.hasNext()){
            val targetRoutes = mutableListOf<Route>()
            val target = scope.peek()
            target?.apply {
                if (target == "*") {
                    targetRoutes.addAll(routes.values)
                    scope.next()
                }
                else
                {
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
}