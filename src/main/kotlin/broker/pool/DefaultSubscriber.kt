package broker.pool

import broker.Scope
import protocol.RoutedMessage
import java.util.*

class DefaultSubscriber(vararg scopes : Scope): Subscriber {
    override val uid: String = UUID.randomUUID().toString()
    override val scopes: MutableList<Scope> = scopes.toMutableList()

    override fun messagePublished(scope: Scope, message: RoutedMessage) {

    }
}