package broker.queue

import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class TemporaryExtendedQueue<T>(queue: Queue<T>, name : String) : AbstractExtendedQueue<T>(queue, name),
ExtendedMortalQueue<T>{

    override val type: QueueType
        get() = QueueType.TEMPORARY

    constructor(name : String) : this(LinkedBlockingQueue<T>(),name)

    override val shouldDie: Boolean
        get() = queue.size == 0
}