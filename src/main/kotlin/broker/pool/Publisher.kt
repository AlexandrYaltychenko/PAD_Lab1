package broker.pool

import broker.Scope
import protocol.RoutedMessage

interface Publisher {
    val uid : String
    val isDead : Boolean
    val interval : Long
    var lastWill : RoutedMessage?
    val scope : Scope
    fun confirm()
}