package broker.pool

import protocol.RoutedMessage

interface Publisher {
    val uid : String
    val isDead : Boolean
    val interval : Long
    var lastWill : RoutedMessage?
    fun confirm()
}