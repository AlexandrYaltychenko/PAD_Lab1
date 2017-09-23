package broker.queue

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oracle.javafx.jmx.json.JSONException
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


class DefaultExtendedQueue<T> constructor(private val queue: Queue<T>, override val name: String, tryToReload: Boolean = true) : ExtendedQueue<T>, Queue<T> by queue {
    constructor(name: String, tryToReload: Boolean = true) : this(LinkedBlockingQueue<T>(), name, tryToReload)

    init {
        if (tryToReload)
            load("$name.txt")
    }

    override fun encode(): String {
        return Gson().toJson(queue)
    }

    override fun decode(str: String): Boolean {
        val turnsType = object : TypeToken<Queue<T>>() {}.type
        return try {
            val loaded: Queue<T> = Gson().fromJson<Queue<T>>(str, turnsType)
            queue.addAll(loaded)
            println("$name successfully deserialized (${queue.size} items)")
            true
        } catch (e: JSONException) {
            println("$name deserialization failed")
            false
        }
    }

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
                save("$name.txt")
                afterBackupItemsCount = 0
                lastBackUp = System.currentTimeMillis()
                println("$name backup in file $name.txt")
            } catch (e: IOException) {
                println("backup of $name failed...")
            }
        }

    }

    @Synchronized override fun save(fileName: String) {
        File(fileName).printWriter().use { out ->
            synchronized(this) {
                out.write(encode())
            }
        }
    }

    override fun load(fileName: String) {
        println("trying to load $name queue from $name.txt...")
        decode(File(fileName).bufferedReader().use { it.readText() })
    }

    override var backUpItemsLimit: Int = DEFAULT_BACKUP_LIMIT
    override var backUpInterval: Long = DEFAULT_BACKUP_INTERVAL
    override var lastBackUp: Long = System.currentTimeMillis()
    override var afterBackupItemsCount: Int = 0
        private set
}