package broker.pool

import protocol.RoutedMessage

class DefaultPublisher (override val uid : String, override val interval : Long = 10000) : Publisher{
    private var lastMsg : Long = System.currentTimeMillis()
    override var lastWill: RoutedMessage? = null
    override val isDead: Boolean
        get() = System.currentTimeMillis() - lastMsg > interval

    override fun confirm() {
       lastMsg = System.currentTimeMillis()
    }
}