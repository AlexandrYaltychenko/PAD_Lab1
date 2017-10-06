package protocol

data class RoutedMessage(val clientType: ClientType = ClientType.PUBLISHER,
                         val clientUid: String = "",
                         val payload: String = "",
                         val topic: String = "main",
                         val messageType: MessageType = MessageType.NORMAL)