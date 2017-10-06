package broker.pool

import broker.Scope
import protocol.Connection
import protocol.RoutedMessage
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket

interface Subscriber {
    val uid : String
    val scopes : List<Scope>
    val isAttached : Boolean
    suspend fun messagePublished(scope : Scope, message : RoutedMessage)
    suspend fun handle(connection : Connection)
    suspend fun stop()
}