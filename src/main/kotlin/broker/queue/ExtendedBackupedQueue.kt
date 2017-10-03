package broker.queue

import com.google.gson.reflect.TypeToken
import protocol.Protocol
import java.util.*

interface ExtendedBackupedQueue<T> : ExtendedQueue<T> {
    fun save(fileName: String) {}
    fun load(fileName: String, clazz : TypeToken<Queue<T>>) {}
    fun backUp(force: Boolean) {}
    fun shouldDestroy() = false
    var backUpItemsLimit: Int
    var backUpInterval: Long
    val lastBackUp: Long
    val afterBackupItemsCount: Int
    val DEFAULT_BACKUP_LIMIT : Int
        get() = Protocol.DEFAULT_BACKUP_LIMIT
    val DEFAULT_BACKUP_INTERVAL : Long
        get() = Protocol.DEFAULT_BACKUP_INTERVAL
}