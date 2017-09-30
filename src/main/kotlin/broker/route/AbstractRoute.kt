package broker.route

import java.util.concurrent.ConcurrentHashMap

abstract class AbstractRoute : Route {
    protected val routes : Map<String, Route> = ConcurrentHashMap()
}