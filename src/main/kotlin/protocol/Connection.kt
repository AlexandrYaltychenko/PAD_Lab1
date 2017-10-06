package protocol

import com.google.gson.JsonSyntaxException
import util.asRoutedMessage
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Connection(val socket: Socket, val reader: BufferedReader, val writer: PrintWriter) {

    constructor(client: Socket) : this(client, BufferedReader(InputStreamReader(client.inputStream)),
            PrintWriter(client.outputStream))

    fun writeMsg(msg: RoutedMessage) {
        writer.println(msg.encode())
        writer.flush()
    }

    fun readMsg(): RoutedMessage? {
        try {
            return reader.readLine().asRoutedMessage()
        } catch (e: Exception) {
            return null
        }
    }

    fun close() {
        if (isClosed)
            return
        writer.close()
        reader.close()
        socket.close()
    }

    val isClosed: Boolean
        get() = socket.isClosed

}