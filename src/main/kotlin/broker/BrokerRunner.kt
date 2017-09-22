package broker

import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking{
    val broker = Broker()
    broker.runServer()
}