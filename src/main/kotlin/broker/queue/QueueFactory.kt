package broker.queue

import com.google.gson.reflect.TypeToken
import protocol.RoutedMessage
import java.util.*

object QueueFactory {
    fun <T> createQueue(type: QueueType, name: String, clazz:
    TypeToken<Queue<T>>? = null): ExtendedQueue<T> {
        return when (type) {
            QueueType.PERMANENT -> if (clazz != null) PermanentExtendedQueue(name, true, clazz) else
                TemporaryExtendedQueue(name)
            QueueType.TEMPORARY -> TemporaryExtendedQueue(name)
        }
    }
}