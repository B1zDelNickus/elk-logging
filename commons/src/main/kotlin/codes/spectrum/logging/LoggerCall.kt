package codes.spectrum.logging

import codes.spectrum.serialization.json.Json
import kotlin.reflect.KCallable

/**
 *  Обертка над абстрактным вызовом лога, но в увязке с SLF
 */
data class LoggerCall(
    /**
     * Строка сообщения
     */
    var message: String = "",
    /**
     * Исключение
     */
    var error: Throwable? = null,
    /**
     * Маркер (может поддерживаться SLF)
     */
    var marker: String? = null,
    /**
     * Аргументы для форматирования
     */
    var args: List<Any>? = null
) {
    init {
        if (message.isBlank() && null != error) {
            message = "${error!!.javaClass.name} - ${error!!.message}"
        }
    }

    companion object {
        /**
         * Превращает входной объект в LoggerCall
         */
        fun resolve(obj: Any): LoggerCall {
            if (obj is LoggerCall) return obj
            if (obj is String) return LoggerCall(obj)
            if (obj is KCallable<*>) return resolve(obj.call() ?: Any())
            if (obj is () -> Any?) return resolve(obj.invoke() ?: Any())
            if (obj is Throwable) return LoggerCall(
                message = "${obj.javaClass.name} - ${obj.message}",
                error = obj
            )
            return LoggerCall(message = Json.stringify(obj))
        }
    }
}

