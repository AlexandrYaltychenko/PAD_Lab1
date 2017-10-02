package subscriber

interface ExternalSubscriber {
    suspend fun run()
}