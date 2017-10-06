package broker

import protocol.RoutedMessage

object TopicFactory {
    fun fromString(str : String) : Topic {
        return RouteTopic(str)
    }

    fun fromMessage(msg : RoutedMessage) : Topic {
        return fromString(msg.topic)
    }

    fun appendToEnd(topic: Topic, str : String) : Topic {
        return RouteTopic("$topic.$str")
    }

    fun appendToBegin(topic: Topic, str : String) : Topic {
        return RouteTopic("$str.$topic")
    }
}