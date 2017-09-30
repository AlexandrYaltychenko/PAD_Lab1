package broker

import java.util.*

class RouteScope constructor(scope : String): Scope {
    private val stack : Stack<String> = Stack()
    init {
        stack.addAll(scope.split("."))
        println("Created scope "+stack)
    }
    override fun next(): String? {
        return if (!stack.empty())
            stack.pop()
        else
            null
    }

    override fun hasNext(): Boolean {
        return !stack.empty()
    }

    override fun peek(): String? {
        return if (!stack.empty())
            stack.peek()
        else
            null
    }
}