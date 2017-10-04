package protocol

import com.google.gson.JsonSyntaxException
import util.asRoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

abstract class BaseClient(val uid: String, val port: Int, val host: String) {

    protected fun connect() : Connection {
        val client = Socket(host, port)
        val writer = PrintWriter(client.outputStream)
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        return Connection(client, reader, writer)
    }

    fun sendNoResponseMessage(msg : RoutedMessage) {
        val connection = connect()
        connection.writeMsg(msg)
        connection.close()
    }

    fun sendResponsedMessage(msg : RoutedMessage) : RoutedMessage? {
        val connection = connect()
        connection.writeMsg(msg)
        val response = connection.readMsg()
        connection.close()
        return response
    }
    
    class Connection(val socket: Socket, val reader: BufferedReader, val writer: PrintWriter) {
        fun writeMsg(msg: RoutedMessage) {
            writer.println(msg.encode())
            writer.flush()
        }

        fun readMsg() : RoutedMessage? {
            return try {
                reader.readLine().asRoutedMessage()
            } catch (e : JsonSyntaxException){
                null
            }
        }

        fun close() {
            writer.close()
            reader.close()
            socket.close()
        }

    }
}