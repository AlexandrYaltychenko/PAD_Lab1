package broker

object TopicFactory {
    fun fromListString(root : String, str: String): List<Topic> {
        println("FROM LIST $str")
        if (str.contains(',')) {
            return str.split(',').map { RouteTopic("$root.$it") }
        }
        return listOf(RouteTopic("$root.$str"))
    }

    fun fromString(str : String) : Topic {
        return RouteTopic(str)
    }

    fun appendToEnd(topic: Topic, str: String): Topic {
        return RouteTopic("$topic.$str")
    }

    fun appendToBegin(topic: Topic, str: String): Topic {
        return RouteTopic("$str.$topic")
    }
}