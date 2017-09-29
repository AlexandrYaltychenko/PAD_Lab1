package protocol

data class RoutedMessage(val clientType: ClientType,
                   val clientUid: String = "",
                   val msg: String = "",
                         val scope : String = "main")