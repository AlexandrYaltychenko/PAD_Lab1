package broker.pool

import broker.Scope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.Connection
import protocol.RoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class DefaultSubscriber(private val subscriberPool: SubscriberPool, vararg scopes: Scope, override val uid: String) : Subscriber {
    override val scopes: MutableList<Scope> = scopes.toMutableList()
    private val channel: Channel<RoutedMessage> = Channel()
    private var job: Job? = null
    override var isAttached: Boolean = false

    override suspend fun messagePublished(scope: Scope, message: RoutedMessage) {
        channel.send(message)
    }

    override suspend fun handle(connection: Connection) {
        while (true) {
            val msg = channel.receive()
            connection.writeMsg(msg)
            if (connection.writer.checkError()) {
                break
            }
            delay(250)
        }
        connection.close()
        subscriberPool.unsubscribe(this)
    }

    suspend override fun stop() {
        job?.cancel() //cancelling coroutine
        while (!channel.isEmpty) {
            channel.poll()
        }
    }
}