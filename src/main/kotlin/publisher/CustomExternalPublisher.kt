package publisher

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import protocol.*
import java.util.*

class CustomExternalPublisher(private val clientId: String, private val scope: String) : BaseClient(clientId,
        Protocol.PORT_NUMBER,
        Protocol.HOST, ClientType.PUBLISHER), ExternalPublisher {
    private var job : Job? = null
    private suspend fun makeTask() {
        while (true) {
            println("publishing message... to $scope")
            sendNoResponseMessage(createMessage(UUID.randomUUID().toString(), MessageType.NORMAL, scope))
            delay(Protocol.CLIENT_INTERVAL)
        }
    }

    override suspend fun run() {
        println("SENDER $clientId STARTED WORKING...")
        Runtime.getRuntime().addShutdownHook(Thread {
            job?.cancel()
            println("send disconnect signal? (y/n)")
            if (readLine()?.contains("y") == true)  {
                println("sending disconnect message")
                sendNoResponseMessage(createMessage("I decided to disconnect...", MessageType.DISCONNECT, scope))
            }
            println("SENDER $clientId STOPPED WORKING")
        })
        if (initConnection()) {
            job = launch(CommonPool) {
                makeTask()
            }
            job?.join()
        } else
            println("failed to establish connection with the server...")
    }

    private fun initConnection(): Boolean {
        var response: RoutedMessage? = sendResponsedMessage(createMessage(Protocol.DEFAULT_PUBLISHER_INTERVAL.toString(), MessageType.CONNECT, scope)) ?: return false
        println(response)
        response = sendResponsedMessage(createMessage("Something made me not responding... Alarm!", MessageType.LAST_WILL, scope))
        println(response)
        if (response == null)
            return false
        return true
    }
}