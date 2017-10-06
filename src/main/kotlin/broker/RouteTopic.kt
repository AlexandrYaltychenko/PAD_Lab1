package broker

import java.util.*

class RouteTopic constructor(scope: String) : Topic {
    private val stack: Stack<String> = Stack()
    private val str: String = scope
    override val last: String
        get() = if (stack.size > 0) stack[0] else ""

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

    override fun belongsTo(topics: Collection<Topic>): TopicRelationship {
        //println("CALLED BELONGS")
        var current = TopicRelationship.ABORT
        for (scope in topics) {
            val relationship = compatible(scope)
            //println("GOT = " + relationship)
            when (relationship) {
                TopicRelationship.NOT_INCLUDED ->
                    if (current == TopicRelationship.ABORT)
                        current = TopicRelationship.NOT_INCLUDED
                    else
                        if (current == TopicRelationship.FINAL)
                            current = TopicRelationship.INCLUDED
                TopicRelationship.FINAL ->
                    if (current == TopicRelationship.ABORT)
                        current = TopicRelationship.FINAL
                    else
                        if (current == TopicRelationship.NOT_INCLUDED)
                            current = TopicRelationship.INCLUDED
                TopicRelationship.INCLUDED ->
                    return TopicRelationship.INCLUDED
            }
        }
        return current
    }

    /**
     * compares two topics
     * first one is final
     * @return TopicRelationship
     */
    override fun compatible(topic: Topic): TopicRelationship {
        //println("checking compatibility ${this.toList()} with ${topic.toList()}")
        val scopeList = toList()
        val anotherScopeList = topic.toList()
        for (it in 0 until minOf(scopeList.size, anotherScopeList.size)) {
            //println("it = "+it)
            if (scopeList[it] != "*" &&
                    anotherScopeList[it] != "*" &&
                    scopeList[it] != anotherScopeList[it]) {
                //println("ABORTING ${scopeList[it]} - ${anotherScopeList[it]}")
                return TopicRelationship.ABORT
            }
        }
        if (scopeList.size == anotherScopeList.size) {
            if (anotherScopeList.last() == "*")
                return TopicRelationship.INCLUDED
            else
                return TopicRelationship.FINAL
        } else if (scopeList.size > anotherScopeList.size) {
            if (anotherScopeList.last() == "*")
                return TopicRelationship.INCLUDED
            else
                return TopicRelationship.ABORT
        } else {
            return TopicRelationship.NOT_INCLUDED
            /*if (anotherScopeList[scopeList.lastIndex] == "*")
                return TopicRelationship.INCLUDED
            else
                return TopicRelationship.NOT_INCLUDED*/
        }
    }

    override fun equals(other: Any?): Boolean {
        val scope = other as? RouteTopic ?: return false
        return scope.toString() == str
    }

    override fun relationToSet(topics: Collection<Topic>): TopicRelationship {
        return belongsTo(topics)
    }

    override val levelsCount: Int
        get() = stack.size
}