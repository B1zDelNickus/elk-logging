package codes.spectrum.logging.slf4j.interception

import codes.spectrum.logging.LoggerMessage
import codes.spectrum.logging.slf4j.internals.unifyArg
import codes.spectrum.logging.slf4j.isEnabled
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level

/**
 * Реализация SLF Logger для записи объектов логинга в память
 * может использовать для проксирования другого целевого логгера
 */
class InterceptionLogger(
    /**
     * Целевой логгер, в который может проксироваться вызов, не обязателен
     */
    val innerLogger: Logger? = null,
    /**
     * Собственный общий минимальный уровень - если установлен - то перекрывает
     * уровень innerLogger, если сообщение не проходит этот уровень - оно не пишется
     * ни в память, не уходит и на innerLogger
     */
    val level: Level? = null,
    /**
     * Собственное имя логгера, если установлено перекрывает innerLogger
     */
    val selfName: String? = null,
    /**
     * Уровень для записи в память (может быть выше чем level, не блокирует запись в innerLog)
     * в отличие от level - никак не влияет на запись в innerLog
     */
    val memoryLevel: Level? = null,
    /**
     * Хранилище сообщений - собственно буфер из которого затем можно прочитать накопленные сообщения
     */
    val messages: MutableList<LoggerMessage> = mutableListOf()
) : Logger {


    fun isEnabled(messagelevel: Level, marker: Marker? = null): Boolean {
        if (null != level) return messagelevel.toInt() >= level.toInt()
        if (null != innerLogger) return innerLogger.isEnabled(messagelevel, marker)
        return true
    }

    override fun warn(msg: String?) {
        if (isEnabled(Level.WARN)) {
            addMessage(Level.WARN, LoggerMessage(message = msg ?: ""))
            innerLogger?.warn(msg)
        }
    }

    override fun warn(format: String?, arg: Any?) {
        if (isEnabled(Level.WARN)) {
            addMessage(Level.WARN, LoggerMessage(message = format ?: "", args = listOf(arg)))
            innerLogger?.warn(format, arg)
        }
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.WARN)) {
            addMessage(Level.WARN, LoggerMessage(message = format ?: "", args = unifyArg(arguments)))
            innerLogger?.warn(format, *arguments)
        }
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.WARN)) {
            addMessage(Level.WARN, LoggerMessage(message = format ?: "", args = listOf(arg1, arg2)))
            innerLogger?.warn(format, arg1, arg2)
        }
    }

    override fun warn(msg: String?, t: Throwable?) {
        if (isEnabled(Level.WARN)) {
            addMessage(Level.WARN, LoggerMessage(message = msg ?: "", error = t))
            innerLogger?.warn(msg, t)
        }
    }

    override fun warn(marker: Marker?, msg: String?) {
        if (isEnabled(Level.WARN, marker)) {
            addMessage(Level.WARN, LoggerMessage(message = msg ?: "", marker = marker!!.name))
            innerLogger?.warn(marker, msg)
        }
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        if (isEnabled(Level.WARN, marker)) {
            addMessage(Level.WARN, LoggerMessage(message = format ?: "", marker = marker!!.name, args = listOf(arg)))
            innerLogger?.warn(marker, format, arg)
        }
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.WARN, marker)) {
            addMessage(Level.WARN, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = listOf(arg1, arg2)))
            innerLogger?.warn(marker, format, arg1, arg2)
        }
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.WARN, marker)) {
            addMessage(Level.WARN, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = unifyArg(arguments)))
            innerLogger?.warn(marker, format, *arguments)
        }
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        if (isEnabled(Level.WARN, marker)) {
            addMessage(Level.WARN, LoggerMessage(message = msg ?: "", marker = marker!!.name, error = t))
            innerLogger?.warn(marker, msg, t)
        }
    }

    override fun getName() = selfName ?: innerLogger?.name ?: "NONAME"

    override fun error(msg: String?) {
        if (isEnabled(Level.ERROR)) {
            addMessage(Level.ERROR, LoggerMessage(message = msg ?: ""))
            innerLogger?.error(msg)
        }
    }

    override fun error(format: String?, arg: Any?) {
        if (isEnabled(Level.ERROR)) {
            addMessage(Level.ERROR, LoggerMessage(message = format ?: "", args = listOf(arg)))
            innerLogger?.error(format, arg)
        }
    }

    override fun error(format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.ERROR)) {
            addMessage(Level.ERROR, LoggerMessage(message = format ?: "", args = unifyArg(arguments)))
            innerLogger?.error(format, *arguments)
        }
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.ERROR)) {
            addMessage(Level.ERROR, LoggerMessage(message = format ?: "", args = listOf(arg1, arg2)))
            innerLogger?.error(format, arg1, arg2)
        }
    }

    override fun error(msg: String?, t: Throwable?) {
        if (isEnabled(Level.ERROR)) {
            addMessage(Level.ERROR, LoggerMessage(message = msg ?: "", error = t))
            innerLogger?.error(msg, t)
        }
    }

    override fun error(marker: Marker?, msg: String?) {
        if (isEnabled(Level.ERROR, marker)) {
            addMessage(Level.ERROR, LoggerMessage(message = msg ?: "", marker = marker!!.name))
            innerLogger?.error(marker, msg)
        }
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        if (isEnabled(Level.ERROR, marker)) {
            addMessage(Level.ERROR, LoggerMessage(message = format ?: "", marker = marker!!.name, args = listOf(arg)))
            innerLogger?.error(marker, format, arg)
        }
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.ERROR, marker)) {
            addMessage(Level.ERROR, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = listOf(arg1, arg2)))
            innerLogger?.error(marker, format, arg1, arg2)
        }
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.ERROR, marker)) {
            addMessage(Level.ERROR, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = unifyArg(arguments)))
            innerLogger?.error(marker, format, *arguments)
        }
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        if (isEnabled(Level.ERROR, marker)) {
            addMessage(Level.ERROR, LoggerMessage(message = msg ?: "", marker = marker!!.name, error = t))
            innerLogger?.error(marker, msg, t)
        }
    }

    override fun isErrorEnabled(): Boolean = isEnabled(Level.ERROR)

    override fun isErrorEnabled(marker: Marker?): Boolean {
        return isEnabled(Level.ERROR, marker) && (innerLogger?.isErrorEnabled(marker) ?: true)
    }

    override fun info(msg: String?) {
        if (isEnabled(Level.INFO)) {
            addMessage(Level.INFO, LoggerMessage(message = msg ?: ""))
            innerLogger?.info(msg)
        }
    }

    override fun info(format: String?, arg: Any?) {
        if (isEnabled(Level.INFO)) {
            addMessage(Level.INFO, LoggerMessage(message = format ?: "", args = listOf(arg)))
            innerLogger?.info(format, arg)
        }
    }

    override fun info(format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.INFO)) {
            addMessage(Level.INFO, LoggerMessage(message = format ?: "", args = unifyArg(arguments)))
            innerLogger?.info(format, *arguments)
        }
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.INFO)) {
            addMessage(Level.INFO, LoggerMessage(message = format ?: "", args = listOf(arg1, arg2)))
            innerLogger?.info(format, arg1, arg2)
        }
    }

    override fun info(msg: String?, t: Throwable?) {
        if (isEnabled(Level.INFO)) {
            addMessage(Level.INFO, LoggerMessage(message = msg ?: "", error = t))
            innerLogger?.info(msg, t)
        }
    }

    override fun info(marker: Marker?, msg: String?) {
        if (isEnabled(Level.INFO, marker)) {
            addMessage(Level.INFO, LoggerMessage(message = msg ?: "", marker = marker!!.name))
            innerLogger?.info(marker, msg)
        }
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        if (isEnabled(Level.INFO, marker)) {
            addMessage(Level.INFO, LoggerMessage(message = format ?: "", marker = marker!!.name, args = listOf(arg)))
            innerLogger?.info(marker, format, arg)
        }
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.INFO, marker)) {
            addMessage(Level.INFO, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = listOf(arg1, arg2)))
            innerLogger?.info(marker, format, arg1, arg2)
        }
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.INFO, marker)) {
            addMessage(Level.INFO, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = unifyArg(arguments)))
            innerLogger?.info(marker, format, *arguments)
        }
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        if (isEnabled(Level.INFO, marker)) {
            addMessage(Level.INFO, LoggerMessage(message = msg ?: "", marker = marker!!.name, error = t))
            innerLogger?.info(marker, msg, t)
        }
    }

    override fun isDebugEnabled(): Boolean = isEnabled(Level.DEBUG)

    override fun isDebugEnabled(marker: Marker?): Boolean {
        return isEnabled(Level.DEBUG, marker) && (innerLogger?.isDebugEnabled(marker) ?: true)
    }

    override fun trace(msg: String?) {
        if (isEnabled(Level.TRACE)) {
            addMessage(Level.TRACE, LoggerMessage(message = msg ?: ""))
            innerLogger?.trace(msg)
        }
    }

    override fun trace(format: String?, arg: Any?) {
        if (isEnabled(Level.TRACE)) {
            addMessage(Level.TRACE, LoggerMessage(message = format ?: "", args = listOf(arg)))
            innerLogger?.trace(format, arg)
        }
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.TRACE)) {
            addMessage(Level.TRACE, LoggerMessage(message = format ?: "", args = unifyArg(arguments)))
            innerLogger?.trace(format, *arguments)
        }
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.TRACE)) {
            addMessage(Level.TRACE, LoggerMessage(message = format ?: "", args = listOf(arg1, arg2)))
            innerLogger?.trace(format, arg1, arg2)
        }
    }

    override fun trace(msg: String?, t: Throwable?) {
        if (isEnabled(Level.TRACE)) {
            addMessage(Level.TRACE, LoggerMessage(message = msg ?: "", error = t))
            innerLogger?.trace(msg, t)
        }
    }

    override fun trace(marker: Marker?, msg: String?) {
        if (isEnabled(Level.TRACE, marker)) {
            addMessage(Level.TRACE, LoggerMessage(message = msg ?: "", marker = marker!!.name))
            innerLogger?.trace(marker, msg)
        }
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        if (isEnabled(Level.TRACE, marker)) {
            addMessage(Level.TRACE, LoggerMessage(message = format ?: "", marker = marker!!.name, args = listOf(arg)))
            innerLogger?.trace(marker, format, arg)
        }
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.TRACE, marker)) {
            addMessage(Level.TRACE, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = listOf(arg1, arg2)))
            innerLogger?.trace(marker, format, arg1, arg2)
        }
    }

    override fun trace(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.TRACE, marker)) {
            addMessage(Level.TRACE, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = unifyArg(arguments)))
            innerLogger?.trace(marker, format, *arguments)
        }
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        if (isEnabled(Level.TRACE, marker)) {
            addMessage(Level.TRACE, LoggerMessage(message = msg ?: "", marker = marker!!.name, error = t))
            innerLogger?.trace(marker, msg, t)
        }
    }

    override fun isInfoEnabled(): Boolean = isEnabled(Level.INFO)

    override fun isInfoEnabled(marker: Marker?): Boolean {
        return isEnabled(Level.INFO, marker) && (innerLogger?.isErrorEnabled(marker) ?: true)
    }

    override fun debug(msg: String?) {
        if (isEnabled(Level.DEBUG)) {
            addMessage(Level.DEBUG, LoggerMessage(message = msg ?: ""))
            innerLogger?.debug(msg)
        }
    }

    override fun debug(format: String?, arg: Any?) {
        if (isEnabled(Level.DEBUG)) {
            addMessage(Level.DEBUG, LoggerMessage(message = format ?: "", args = listOf(arg)))
            innerLogger?.debug(format, arg)
        }
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.DEBUG)) {
            addMessage(Level.DEBUG, LoggerMessage(message = format ?: "", args = unifyArg(arguments)))
            innerLogger?.debug(format, *arguments)
        }
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.DEBUG)) {
            addMessage(Level.DEBUG, LoggerMessage(message = format ?: "", args = listOf(arg1, arg2)))
            innerLogger?.debug(format, arg1, arg2)
        }
    }

    override fun debug(msg: String?, t: Throwable?) {
        if (isEnabled(Level.DEBUG)) {
            addMessage(Level.DEBUG, LoggerMessage(message = msg ?: "", error = t))
            innerLogger?.debug(msg, t)
        }
    }

    override fun debug(marker: Marker?, msg: String?) {
        if (isEnabled(Level.DEBUG, marker)) {
            addMessage(Level.DEBUG, LoggerMessage(message = msg ?: "", marker = marker!!.name))
            innerLogger?.debug(marker, msg)
        }
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        if (isEnabled(Level.DEBUG, marker)) {
            addMessage(Level.DEBUG, LoggerMessage(message = format ?: "", marker = marker!!.name, args = listOf(arg)))
            innerLogger?.debug(marker, format, arg)
        }
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isEnabled(Level.DEBUG, marker)) {
            addMessage(Level.DEBUG, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = listOf(arg1, arg2)))
            innerLogger?.debug(marker, format, arg1, arg2)
        }
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isEnabled(Level.DEBUG, marker)) {
            addMessage(Level.DEBUG, LoggerMessage(message = format
                ?: "", marker = marker!!.name, args = unifyArg(arguments)))
            innerLogger?.debug(marker, format, *arguments)
        }
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        if (isEnabled(Level.DEBUG, marker)) {
            addMessage(Level.DEBUG, LoggerMessage(message = msg ?: "", marker = marker!!.name, error = t))
            innerLogger?.debug(marker, msg, t)
        }
    }

    override fun isWarnEnabled(): Boolean = isEnabled(Level.WARN)

    override fun isWarnEnabled(marker: Marker?): Boolean {
        return isEnabled(Level.WARN, marker) && (innerLogger?.isErrorEnabled(marker) ?: true)
    }

    override fun isTraceEnabled(): Boolean = isEnabled(Level.TRACE)

    override fun isTraceEnabled(marker: Marker?): Boolean {
        return isEnabled(Level.TRACE, marker) && (innerLogger?.isErrorEnabled(marker) ?: true)
    }


    private fun addMessage(level: Level, message: LoggerMessage) {
        if (null == memoryLevel || level >= memoryLevel) {
            message.level = level
            message.logName = name
            messages.add(message)
        }

    }


}