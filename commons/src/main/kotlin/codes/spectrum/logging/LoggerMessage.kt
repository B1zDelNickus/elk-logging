package codes.spectrum.logging

import org.slf4j.event.Level
import java.util.*

/**
 * Структура для отображения реально созданного объекта логирования внутри системы
 */
data class LoggerMessage(
    /**
     * Уровень запись
     */
    var level: Level? = null,

    /**
     * Имя журнала
     */
    var logName: String? = null,

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
    var args: List<Any?>? = null,

    var timestemap: Long = 0,

    var threadName: String = ""
) {
    init {
        if (timestemap == 0L) timestemap = Date().time
        if (threadName.isBlank()) threadName = Thread.currentThread().name
    }
}