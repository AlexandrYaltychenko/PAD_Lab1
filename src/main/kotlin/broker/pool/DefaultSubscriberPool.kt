package broker.pool

import broker.TopicFactory
import broker.route.PermanentRoute
import broker.route.Route
import protocol.RoutedMessage

class DefaultSubscriberPool (private val root : Route = PermanentRoute(TopicFactory.fromString("root"), "root")) : SubscriberPool, Route by root {

    override suspend fun notify(msg: RoutedMessage) {
        root.putMessage(TopicFactory.fromString(msg.topic), msg)
    }

}