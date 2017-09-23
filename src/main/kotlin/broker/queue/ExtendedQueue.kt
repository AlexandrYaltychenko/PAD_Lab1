package broker.queue

import java.util.*

interface ExtendedQueue<T> : Queue<T> {
    fun encode(): String
    fun decode(str: String): Boolean
    fun save(fileName: String)
    fun load(fileName: String)
    /**
     * Creates backup if time elapsed or afterBackUpItemsCount exceeds the Limit
     * @param force if true backup is created even if
     */
    fun backUp(force: Boolean)

    var backUpItemsLimit: Int
    var backUpInterval: Long
    val name: String
    val lastBackUp: Long
    val afterBackupItemsCount: Int
    val DEFAULT_BACKUP_LIMIT
        get() = 100
    val DEFAULT_BACKUP_INTERVAL : Long
        get() = 60000

}