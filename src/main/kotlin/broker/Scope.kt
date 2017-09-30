package broker

interface Scope {
    fun next() : String?
    fun peek() : String?
    fun hasNext() : Boolean
}