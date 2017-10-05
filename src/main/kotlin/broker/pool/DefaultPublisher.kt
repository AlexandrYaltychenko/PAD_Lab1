package broker.pool

import broker.Scope
import broker.ScopeFactory
import protocol.RoutedMessage

class DefaultPublisher(override val uid: String, override val interval: Long = 10000, scope: String) : Publisher {
    private var lastMsg: Long = System.currentTimeMillis()
    override var lastWill: RoutedMessage? = null
    override val scope: Scope = ScopeFactory.fromString(scope)
    override val isDead: Boolean
        get() : Boolean {
            println("checking death... ")
            return System.currentTimeMillis() - lastMsg > interval
        }

    override fun confirm() {
        println("publisher confirmed")
        lastMsg = System.currentTimeMillis()
    }
}