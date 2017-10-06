package broker

enum class TopicRelationship {
    ABORT, //not compatible
    NOT_INCLUDED, //compatible with possible sub-routes
    INCLUDED, //compatible with given route and possible sub-routes
    FINAL //compatible with route
}