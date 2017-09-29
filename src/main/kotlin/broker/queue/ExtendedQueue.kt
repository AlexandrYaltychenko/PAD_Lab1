package broker.queue

import java.util.*

interface ExtendedQueue<T> : Queue<T> {
    val name: String
    val type : QueueType
}