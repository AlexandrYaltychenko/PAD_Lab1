package broker.pool

import broker.Scope
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import protocol.RoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class DefaultSubscriber(vararg scopes: Scope) : Subscriber {
    override val uid: String = UUID.randomUUID().toString()
    override val scopes: MutableList<Scope> = scopes.toMutableList()
    private val channel : Channel<RoutedMessage> = Channel()

    override suspend fun messagePublished(scope: Scope, message: RoutedMessage) {
        println("\nsubscriber $scopes\n got $message from $scope\n")
        channel.send(message)
    }

    override suspend fun handle(client: Socket, reader: BufferedReader, writer: PrintWriter) {
        try {
            while (true) {
                val msg = channel.receive()
                writer.println(msg.encode())
                writer.flush()
                delay(150)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            writer.close()
            reader.close()
        }
    }
}