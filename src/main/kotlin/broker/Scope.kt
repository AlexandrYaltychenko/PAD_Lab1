package broker

interface Scope {
    fun next() : String?
    fun peek() : String?
    fun hasNext() : Boolean
    fun compatible(scope : Scope) : ScopeRelationship
    fun belongsTo(scopes: Collection<Scope>) : ScopeRelationship
    fun relationToSet(scopes: Collection<Scope>) : ScopeRelationship
    fun toList() : List<String>
    val levelsCount : Int
}