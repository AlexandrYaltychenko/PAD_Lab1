package protocol

data class RoutedMessage(val clientType: ClientType,
                         val clientUid: String = "",
                         val payload: String = "",
                         val topic: String = Protocol.DEFAULT_QUEUE)


