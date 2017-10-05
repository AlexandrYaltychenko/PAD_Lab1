package protocol

interface Client {
    fun sendNoResponseMessage(msg : RoutedMessage)
    fun sendResponsedMessage(msg : RoutedMessage) : RoutedMessage?
}