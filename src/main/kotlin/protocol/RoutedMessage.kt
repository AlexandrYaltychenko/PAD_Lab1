package protocol

data class RoutedMessage(val clientType: ClientType = ClientType.PUBLISHER,
                         val clientUid: String = "",
                         val msg: String = "",
                         val scope : String = "main")