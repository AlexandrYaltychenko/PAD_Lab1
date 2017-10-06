package broker

interface Topic {
    fun next() : String?
    fun peek() : String?
    fun hasNext() : Boolean
    fun compatible(topic: Topic) : TopicRelationship
    fun belongsTo(topics: Collection<Topic>) : TopicRelationship
    fun relationToSet(topics: Collection<Topic>) : TopicRelationship
    fun toList() : List<String>
    val levelsCount : Int
    val last : String
}