package broker.pool

import broker.Scope
import protocol.RoutedMessage

interface Subscriber {
    val uid : String
    val scopes : List<Scope>
    fun messagePublished(scope : Scope, message : RoutedMessage)
}