package broker.queue

interface ExtendedMortalQueue<T> : ExtendedQueue<T> {
    val shouldDie : Boolean
}