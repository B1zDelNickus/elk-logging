package codes.spectrum.logging

import codes.spectrum.logging.slf4j.log
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class LoggingContext(
    var logger: Logger = LoggerFactory.getLogger("codes.spectrum"),
    var logobj: LoggingObject = LoggingObject(),
    var startLevel: Level = Level.INFO,
    var finishLevel: Level = startLevel,
    var errorLevel: Level = Level.ERROR,
    var bodyLevel: Level = when (startLevel) {
        Level.INFO -> Level.DEBUG
        Level.DEBUG -> Level.TRACE
        else -> Level.TRACE
    }
) {
    fun trace(message: String = "", setup: (LoggingObject.() -> Unit)? = null) = log(Level.TRACE, message, setup)
    fun debug(message: String = "", setup: (LoggingObject.() -> Unit)? = null) = log(Level.DEBUG, message, setup)
    fun info(message: String = "", setup: (LoggingObject.() -> Unit)? = null) = log(Level.INFO, message, setup)
    fun warn(message: String = "", setup: (LoggingObject.() -> Unit)? = null) = log(Level.WARN, message, setup)
    fun error(message: String = "", setup: (LoggingObject.() -> Unit)? = null) = log(Level.ERROR, message, setup)
    fun log(message: String = "", setup: (LoggingObject.() -> Unit)? = null) {
        uptime()
        log(bodyLevel, message, setup)
    }



    fun setAllLevels(level:Level){
        startLevel = level
        finishLevel = level
        bodyLevel = level
    }

    fun log(level: Level, message: String = "", setup: (LoggingObject.() -> Unit)? = null) {
        logger.log(level) {
            if (message.isNotBlank() || setup != null) {
                logobj.copy(message = "${message} ${logobj.message?:""}".trim()).also {
                    setup?.invoke(it)
                }
            } else {
                logobj
            }
        }
    }

    fun finish() = logobj.finish()
    fun uptime() = logobj.uptime()
    fun <R> execute(body: suspend LoggingContext.() -> R):R {
        log(startLevel)
        try {
            logobj.phase = "EXECUTE"
            logobj.state = "PROGRESS"
            var result = runBlocking { body() }
            logobj.finish()
            log(finishLevel)
            return result
        } catch (e: Throwable) {
            logobj.finish(e)
            log(errorLevel)
            throw e
        }
    }

    fun <R> subOperation(operation: String, setup: LoggingContext.() -> Unit = {}, body: suspend LoggingContext.() -> R):R{
        val childLogobj = logobj.createChild(operation)
        var result :R? = null
        try {
            val baseLevel = if(this.startLevel==Level.TRACE) Level.TRACE else Level.DEBUG
            val childContext = LoggingContext(logger, childLogobj, startLevel = baseLevel, finishLevel = baseLevel, bodyLevel = Level.TRACE, errorLevel = Level.ERROR)
            childContext.setup()
            result = childContext.execute(body)
        } catch (e: Throwable) {
            logobj.inc("errors-${childLogobj.operation}",true)
            throw e
        } finally {
            logobj.add("duration-${childLogobj.operation!!.replace("/","--")}", childLogobj.duration,true)
            logobj.inc("count-${childLogobj.operation!!.replace("/","--")}",true)
        }
        return result!!
    }
}