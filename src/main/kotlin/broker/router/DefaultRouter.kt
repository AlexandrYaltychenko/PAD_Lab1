package broker.router

import broker.queue.*
import com.google.gson.reflect.TypeToken
import protocol.ClientType
import protocol.RoutedMessage
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DefaultRouter : Router {
    private val map: MutableMap<String, ExtendedQueue<RoutedMessage>> = ConcurrentHashMap()

    init {
        map.put("main", QueueFactory.createQueue(QueueType.PERMANENT,
                "main",
                object : TypeToken<Queue<RoutedMessage>>() {}))
    }

    override fun put(msg: RoutedMessage) {
        val target: ExtendedQueue<RoutedMessage> = map[msg.topic] ?: run {
            println("creating temporary queue ${msg.topic}")
            val queue = QueueFactory.createQueue<RoutedMessage>(QueueType.TEMPORARY, msg.topic)
            map.put(msg.topic, queue)
            queue
        }
        target.add(msg)
    }

    override fun get(scope: String): RoutedMessage {
        val queue = map[scope]
        return if (queue != null)
            if (queue.size > 0)
                queue.poll()
            else
                RoutedMessage(ClientType.SERVER, topic = scope, payload = "IDLE")
        else
            RoutedMessage(ClientType.SERVER, topic = scope, payload = "error! no such queue: $scope")
    }

    override fun cron() {
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val queue = entry.value
            when (queue) {
            //backuping permanent queues
                is ExtendedBackupedQueue -> (queue as? ExtendedBackupedQueue)?.backUp(false)
            //Removing temporary queues
                is TemporaryExtendedQueue -> {
                    if (queue.shouldDie) {
                        println("removing temp queue ${entry.key}")
                        iterator.remove()
                    }
                }

            }
        }
    }

    override fun onStop() {
        println("force backuping permanent queues")
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            (iterator.next().value as? ExtendedBackupedQueue)?.backUp(true)
        }
    }
}