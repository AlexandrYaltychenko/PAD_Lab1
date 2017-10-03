package broker.route

import broker.Scope
import broker.queue.ExtendedBackupedQueue
import broker.queue.ExtendedQueue
import broker.queue.PermanentExtendedQueue
import broker.queue.QueueType
import com.google.gson.reflect.TypeToken
import protocol.RoutedMessage
import java.util.*

class PermanentRoute(override val scope: Scope, override val name: String) : AbstractRoute(scope, name) {
    override val type: QueueType
        get() = QueueType.PERMANENT
    override val messages: ExtendedBackupedQueue<RoutedMessage> =
            PermanentExtendedQueue(scope.toString().replace('*','-'),
                    true,object : TypeToken<Queue<RoutedMessage>>(){})


    constructor(scope : Scope) : this (scope,scope.last)

    override fun makeBackUp(force : Boolean) {
        messages.backUp(force)
    }
}
