package protocol

data class Message(val clientType: ClientType,
                   val clientUid: String = "",
                   val payload: String = "")