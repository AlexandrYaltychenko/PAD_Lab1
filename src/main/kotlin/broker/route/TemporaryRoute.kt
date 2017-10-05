package broker.route

import broker.Scope
import broker.queue.ExtendedQueue
import broker.queue.PermanentExtendedQueue
import broker.queue.QueueType
import broker.queue.TemporaryExtendedQueue
import com.google.gson.reflect.TypeToken
import protocol.RoutedMessage
import java.util.*

class TemporaryRoute(override val scope: Scope, override val name: String) : AbstractRoute(scope, name) {
    override val type: QueueType
        get() = QueueType.TEMPORARY
    override val messages: ExtendedQueue<RoutedMessage> = TemporaryExtendedQueue(name)

    override fun makeBackUp(force : Boolean) {
        //doing nothing, because the route is temporary
    }
}