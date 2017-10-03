package publisher

interface ExternalPublisher {
    suspend fun run()
}