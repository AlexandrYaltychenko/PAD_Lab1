package broker.pool

import broker.Topic
import broker.TopicFactory
import protocol.RoutedMessage

class DefaultPublisher(override val uid: String, override val interval: Long = 10000, scope: String) : Publisher {
    private var lastMsg: Long = System.currentTimeMillis()
    override var lastWill: RoutedMessage? = null
    override val topic: Topic = TopicFactory.fromString(scope)
    override val isDead: Boolean
        get() : Boolean {
            println("checking death... ")
            return System.currentTimeMillis() - lastMsg > interval*1.25
        }

    override fun confirm() {
        println("publisher confirmed")
        lastMsg = System.currentTimeMillis()
    }
}