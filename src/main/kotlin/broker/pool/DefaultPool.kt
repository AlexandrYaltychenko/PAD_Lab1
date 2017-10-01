package broker.pool

import broker.Scope
import broker.ScopeFactory
import broker.route.PermanentRoute
import broker.route.Route
import protocol.RoutedMessage

class DefaultPool : Pool{
    val root : Route = PermanentRoute(ScopeFactory.fromString("root"),"root")

    override fun publish(message: RoutedMessage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subscribe(subscriber: Subscriber) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unsubscribe(subscriber: Subscriber) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}