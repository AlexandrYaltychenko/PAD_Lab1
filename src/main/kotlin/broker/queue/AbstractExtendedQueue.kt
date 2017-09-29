package broker.queue

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oracle.javafx.jmx.json.JSONException
import java.util.*

abstract class AbstractExtendedQueue<T> (protected val queue: Queue<T>, override val name: String) : ExtendedQueue<T>, Queue<T> by queue{
    protected fun encode(): String {
        return Gson().toJson(queue)
    }

    protected fun decode(str: String, clazz : TypeToken<Queue<T>>): Boolean {
        return try {
            val loaded: Queue<T> = Gson().fromJson(str, clazz.type)
            queue.addAll(loaded)
            println("$name successfully deserialized (${queue.size} items)")
            true
        } catch (e: JSONException) {
            println("$name deserialization failed")
            false
        }
    }
}