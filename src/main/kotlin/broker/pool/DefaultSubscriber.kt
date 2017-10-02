package broker.pool

import broker.Scope
import broker.SubscriberPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import protocol.RoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class DefaultSubscriber(private val subscriberPool: SubscriberPool, vararg scopes: Scope) : Subscriber {
    override val uid: String = UUID.randomUUID().toString()
    override val scopes: MutableList<Scope> = scopes.toMutableList()
    private val channel: Channel<RoutedMessage> = Channel()

    override suspend fun messagePublished(scope: Scope, message: RoutedMessage) {
        //println("\nsubscriber $scopes\n got $message from $scope")
        channel.send(message)
    }

    override suspend fun handle(client: Socket, reader: BufferedReader, writer: PrintWriter) {
        while (true) {
            val msg = channel.receive()
            writer.println(msg.encode())
            writer.flush()
            if (writer.checkError()) {
                break
            }
            delay(250)
        }
        writer.close()
        reader.close()
        client.close()
        subscriberPool.unsubscribe(this)
    }
}