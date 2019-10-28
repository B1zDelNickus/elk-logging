package codes.spectrum.logging

import codes.spectrum.serialization.json.AnyMap
import codes.spectrum.serialization.json.serializers.ThrowableDescriptor
import com.google.gson.annotations.SerializedName
import org.slf4j.event.Level
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.HashMap

private val HOST  = java.net.InetAddress.getLocalHost().hostName
private val SERVICE_NAME  = System.getenv("SERVICE_NAME")?:"no-service"

data class LoggingObject(
    /**
     * Установить если есть корректное имя сессии
     */
    var sessionId: String? = null,
    /**
     * Установить если есть корректное имя пользователя
     */
    var userId: String? = null,
    /**
     * Установить если есть корректное имя имперсонирующего пользователя
     */
    var impersonatorId: String? = null,
    /**
     * Установить если есть корректный ид реквеста
     */
    var requestId: String? = null,
    /**
     * Установить если есть корректное имя задачи
     */
    var taskId: String? = null,

    /**
     * Установить название операции
     */
    var operation: String? = null,

    /*
     * Установить имя фазы в составе операции
     */
    var phase: String? = "INIT",
    /**
     * Некий статус
     */
    var state: String? = "CREATED",

    var count: AtomicLong = AtomicLong(1L),
    var error_count:AtomicLong = AtomicLong(0L),

    var message: String? = null,

    var created: Date = Date(),
    var duration: Long = 0L,

    var finished: Date? = null,
    var error: Throwable? = null,
    var nestLevel: Int? = null,
    val values: AnyMap = AnyMap(),
    var counters: CounterCollection? = null,
    @SerializedName("@timestamp")
    var timestamp: Date? = null,
    var hostName : String? = null,
    var serviceName : String? = null,
    var level: Level? = null,
    var logger: String? = null,
    var timeFromStart: Long? = null,
    var thread:String? = null
) {
    @Transient
    var parent: LoggingObject? = null

    fun uptime() {
        duration = Date().time - created.time
    }

    fun createChild(operation: String=""): LoggingObject {
        val realoperation = if(operation.isBlank()) this.operation else (
            if (this.operation.isNullOrBlank()) operation else "${this.operation}/${operation}"
            )
        val result = this.copy(phase = "INIT", state = "CREATED", created = Date(), operation = realoperation, count = AtomicLong(1L), duration = 0L, counters = null, values = AnyMap())
        result.parent = this
        return result
    }

    fun finish(e: Throwable? = null) {
        if(null!=finished)return
        finished = Date()
        duration = finished!!.time - created.time
        phase = "COMPLETE"
        if (e != null) {
            error = e
            state = "ERROR"
            error_count.incrementAndGet()
        } else {
            state = "COMPLETE"
        }
    }

    private val _counters: CounterCollection
        get() {
            if (null == counters) {
                synchronized(this) {
                    if (null == counters) counters = CounterCollection()
                }
            }
            return counters!!
        }

    fun check(name: String):Long = counters?.check(name) ?: 0L
    fun inc(name: String, propagate: Boolean = false):Long {
        val result = _counters.add(name, 1)
        if(propagate){
            parent?.let {
                it.inc(name,propagate)
            }
        }
        return result
    }
    fun add(name: String, increment: Long, propagate:Boolean = false): Long {
        val result = _counters.add(name, increment)
        if(propagate) parent?.add(name,increment,propagate)
        return result
    }
    fun add(name: String, increment: Int, propagate:Boolean = false): Long {
        val result = _counters.add(name, increment.toLong())
        if(propagate) parent?.add(name,increment,propagate)
        return result
    }
    fun set(name:String, value:Any, propagate: Boolean = false){
        this.values[name] = value
        if(propagate){
            parent?.set(name,value,propagate)
        }
    }

}