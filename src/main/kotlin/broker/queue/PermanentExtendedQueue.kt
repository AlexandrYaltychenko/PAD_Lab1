package broker.queue

import com.google.gson.reflect.TypeToken
import protocol.Protocol
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


class PermanentExtendedQueue<T>(queue: Queue<T>, name: String, tryToReload: Boolean = true, type : TypeToken<Queue<T>>) :
        AbstractExtendedQueue<T>(queue,name), ExtendedBackupedQueue<T> {
    constructor(name: String, tryToReload: Boolean = true, clazz : TypeToken<Queue<T>>) : this(LinkedBlockingQueue<T>(), name, tryToReload, clazz)

    init {
        if (tryToReload)
            load("$name.json", type)
    }

    override val type: QueueType
        get() = QueueType.PERMANENT

    override fun offer(e: T): Boolean {
        afterBackupItemsCount++
        return queue.offer(e)
    }

    override fun add(element: T): Boolean {
        afterBackupItemsCount++
        return queue.add(element)
    }

    override fun backUp(force: Boolean) {
        if (force ||
                (System.currentTimeMillis() - lastBackUp >= backUpInterval) ||
                (afterBackupItemsCount >= backUpItemsLimit && queue.size > 0)) {
            try {
                save("$name.json")
                afterBackupItemsCount = 0
                lastBackUp = System.currentTimeMillis()
                println("$name backup in file $name.json")
            } catch (e: IOException) {
                println("backup of $name failed...")
            }
        }

    }

    @Synchronized override fun save(fileName: String) {
        File(Protocol.BACKUP_DIR+fileName).printWriter().use { out ->
            synchronized(this) {
                out.write(encode())
            }
        }
    }

    override fun load(fileName: String, clazz : TypeToken<Queue<T>>) {
        println("trying to load $name queue from $name.json...")
        try {
            decode(File(Protocol.BACKUP_DIR+fileName).bufferedReader().use { it.readText() }, clazz)
        } catch (e : IOException){
            //println("loading failed...")
        }
    }

    override var backUpItemsLimit: Int = DEFAULT_BACKUP_LIMIT
    override var backUpInterval: Long = DEFAULT_BACKUP_INTERVAL
    override var lastBackUp: Long = System.currentTimeMillis()
    override var afterBackupItemsCount: Int = 0
        private set
}