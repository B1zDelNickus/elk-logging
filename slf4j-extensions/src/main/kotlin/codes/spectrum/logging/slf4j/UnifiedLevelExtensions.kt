package codes.spectrum.logging.slf4j

import codes.spectrum.logging.ILogLevelProvider
import codes.spectrum.logging.LoggerCall
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.slf4j.event.Level

/**
 * Унифицированная проверка на доступность уровня логирования (SLF Level)
 */
fun Logger.isEnabled(level: Level): Boolean = when (level) {
    Level.TRACE -> isTraceEnabled
    Level.DEBUG -> isDebugEnabled
    Level.INFO -> isInfoEnabled
    Level.WARN -> isWarnEnabled
    Level.ERROR -> isErrorEnabled
}

/**
 * Унифицированная проверка на доступность уровня логирования (SLF Level)
 */
fun Logger.isEnabled(level: Level, marker: String): Boolean {
    if (marker.isBlank()) return isEnabled(level)
    val slfmarker = MarkerFactory.getMarker(marker)
    return isEnabled(level, slfmarker)
}

/**
 * Унифицированная проверка на доступность уровня логирования (SLF Level)
 */
fun Logger.isEnabled(level: Level, marker: Marker? = null): Boolean {
    if (null == marker) return isEnabled(level)
    return when (level) {
        Level.TRACE -> isTraceEnabled(marker)
        Level.DEBUG -> isDebugEnabled(marker)
        Level.INFO -> isInfoEnabled(marker)
        Level.WARN -> isWarnEnabled(marker)
        Level.ERROR -> isErrorEnabled(marker)
    }
}

/**
 * Унифицированная запись в лог подготовленного LoggerCall (SLF Level)
 */
fun Logger.log(level: Level, message: LoggerCall): Boolean {
    when (level) {
        Level.TRACE -> trace(message)
        Level.DEBUG -> debug(message)
        Level.INFO -> info(message)
        Level.WARN -> warn(message)
        Level.ERROR -> error(message)
    }
    return true
}


/**
 * Ленивая унифицированная запись в лог - вызов ламбды только при доступности уровня логирования (SLF Level)
 */
fun Logger.log(level: Level, body: () -> Any): Boolean {
    return if (isEnabled(level)) {
        log(level, body())
    } else false
}


/**
 * Унифицированная запись в лог произвольного объекта с автоопределением уровня (SLF Level)
 */
fun Logger.log(message: Any): Boolean {
    val level = when {
        message is ILogLevelProvider -> message.getLogLevel()
        else -> when (message) {
            is Throwable -> Level.ERROR
            else -> Level.INFO
        }
    }
    return log(level, message)
}

/**
 * Унифицированная запись в лог в сигнатуре String+Arg
 */
fun Logger.log(level: Level, message: String, arg1: Any): Boolean =
    log(level, LoggerCall(message = message, args = listOf(arg1)))

/**
 * Унифицированная запись в лог в сигнатуре String+Arg1+Arg2
 */
fun Logger.log(level: Level, message: String, arg1: Any, arg2: Any): Boolean =
    log(level, LoggerCall(message = message, args = listOf(arg1, arg2)))

/**
 * Унифицированная запись в лог в сигнатуре String+Vararg
 */
fun Logger.log(level: Level, message: String, vararg args: Any): Boolean =
    log(level, LoggerCall(message = message, args = args.toList()))

/**
 * Унифицированная запись в лог произвольного объекта (SLF Level)
 */
fun Logger.log(level: Level, message: Any): Boolean {
    if (message is () -> Any?) {
        return log(level, { message() ?: Any() })
    } else {
        return log(level, LoggerCall.resolve(message))
    }
}

/**
 * Ленивый trace
 */
fun Logger.trace(body: () -> Any): Boolean = this.log(Level.TRACE, body)

/**
 * Ленивый debug
 */
fun Logger.debug(body: () -> Any): Boolean = this.log(Level.DEBUG, body)

/**
 * Ленивый info
 */
fun Logger.info(body: () -> Any): Boolean = this.log(Level.INFO, body)

/**
 * Ленивый warn
 */
fun Logger.warn(body: () -> Any): Boolean = this.log(Level.WARN, body)

/**
 * Ленивый error
 */
fun Logger.error(body: () -> Any): Boolean = this.log(Level.ERROR, body)

/**
 * trace для произвольного объекта
 */
fun Logger.trace(message: Any): Boolean = this.log(Level.TRACE, message)

/**
 * debug для произвольного объекта
 */
fun Logger.debug(message: Any): Boolean = this.log(Level.DEBUG, message)

/**
 * info для произвольного объекта
 */
fun Logger.info(message: Any): Boolean = this.log(Level.INFO, message)

/**
 * warn для произвольного объекта
 */
fun Logger.warn(message: Any): Boolean = this.log(Level.WARN, message)

/**
 * error для произвольного объекта
 */
fun Logger.error(message: Any): Boolean = this.log(Level.ERROR, message)


