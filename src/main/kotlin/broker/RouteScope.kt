package broker

import java.util.*

class RouteScope constructor(scope: String) : Scope {
    private val stack: Stack<String> = Stack()
    private val str: String = scope

    init {
        stack.addAll(scope.split(".").filter { it.isNotEmpty() }.reversed())
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

    override fun toString(): String {
        return str
    }

    override fun toList(): List<String> {
        return stack.toList().reversed()
    }

    override fun belongsTo(scopes: Collection<Scope>): ScopeRelationship {
        var current = ScopeRelationship.ABORT
        for (scope in scopes) {
            val relationship = compatible(scope)
            println("GOT = "+relationship)
            when (compatible(scope)) {
                ScopeRelationship.NOT_INCLUDED ->
                    if (current == ScopeRelationship.ABORT)
                        current = ScopeRelationship.NOT_INCLUDED
                    else
                        if (current == ScopeRelationship.FINAL)
                            current = ScopeRelationship.INCLUDED
                ScopeRelationship.FINAL ->
                    if (current == ScopeRelationship.ABORT)
                        current = ScopeRelationship.FINAL
                    else
                        if (current == ScopeRelationship.NOT_INCLUDED)
                            current = ScopeRelationship.INCLUDED
                ScopeRelationship.INCLUDED ->
                    return ScopeRelationship.INCLUDED
            }
        }
        return current
    }

    /**
     * compares two scopes
     * first one is final
     * @return ScopeRelationship
     */
    override fun compatible(scope: Scope): ScopeRelationship {
        println("checking compatibility ${this.toList()} with ${scope.toList()}")
        val scopeList = toList()
        val anotherScopeList = scope.toList()
        for (it in 0 until scopeList.size - 1) {
            if (scopeList[it] != "*" &&
                    anotherScopeList[it] != "*" &&
                    scopeList[it] != anotherScopeList[it]) {
                println("ABORTING ${scopeList[it]} - ${anotherScopeList[it]}")
                return ScopeRelationship.ABORT
            }
        }
        if (anotherScopeList.size == scopeList.size)
            if (anotherScopeList[scopeList.size - 1] != "*")
                return ScopeRelationship.FINAL
            else
                return ScopeRelationship.INCLUDED
        else
            return ScopeRelationship.NOT_INCLUDED
    }

    override fun equals(other: Any?): Boolean {
        val scope = other as? RouteScope ?: return false
        return scope.toString() == str
    }

    override fun relationToSet(scopes: Collection<Scope>): ScopeRelationship {
        return belongsTo(scopes)
    }

    override val levelsCount: Int
        get() = stack.size
}