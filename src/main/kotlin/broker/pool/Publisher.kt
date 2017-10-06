package broker.pool

import broker.Topic
import protocol.RoutedMessage

interface Publisher {
    val uid : String
    val isDead : Boolean
    val interval : Long
    var lastWill : RoutedMessage?
    val topic: Topic
    fun confirm()
}