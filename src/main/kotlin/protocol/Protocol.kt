package protocol

object Protocol {
    val PORT_NUMBER = 14141
    val HOST = "127.0.0.1"
    val DEFAULT_ROUTE = ""
    val CRON_INTERVAL: Long = 5000
    val CRON_DELAY: Long = 5000
    val CLIENT_INTERVAL: Long = 1000
    val DEFAULT_BACKUP_INTERVAL : Long = 60000
    val DEFAULT_BACKUP_LIMIT : Int = 100
    val DEFAULT_PUBLISHER_INTERVAL = 15000
    val BACKUP_DIR = "backup/"
}