package protocol

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

abstract class BaseClient(val uid: String, val port: Int, val host: String, val clientType: ClientType) : Client{

    protected fun connect(): Connection {
        val client = Socket(host, port)
        val writer = PrintWriter(client.outputStream)
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        return Connection(client, reader, writer)
    }

    override fun sendNoResponseMessage(msg: RoutedMessage) {
        val connection = connect()
        connection.writeMsg(msg)
        connection.close()
    }

    override fun sendResponsedMessage(msg: RoutedMessage): RoutedMessage? {
        val connection = connect()
        connection.writeMsg(msg)
        val response = connection.readMsg()
        connection.close()
        return response
    }

    fun createMessage(payload: String, messageType: MessageType, topic: String = Protocol.DEFAULT_ROUTE): RoutedMessage {
        return RoutedMessage(clientType, uid, payload, topic,messageType)
    }

}