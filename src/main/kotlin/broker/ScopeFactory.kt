package broker

import protocol.RoutedMessage

object ScopeFactory {
    fun fromString(str : String) : Scope {
        return RouteScope(str)
    }

    fun fromMessage(msg : RoutedMessage) : Scope {
        return fromString(msg.scope)
    }

    fun appendToEnd(scope : Scope, str : String) : Scope {
        return RouteScope("$scope.$str")
    }

    fun appendToBegin(scope : Scope, str : String) : Scope {
        return RouteScope("$str.$scope")
    }
}