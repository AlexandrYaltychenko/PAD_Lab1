package protocol

data class RoutedMessage(val clientType: ClientType = ClientType.SENDER,
                         val clientUid: String = "",
                         val msg: String = "",
                         val scope : String = "main")