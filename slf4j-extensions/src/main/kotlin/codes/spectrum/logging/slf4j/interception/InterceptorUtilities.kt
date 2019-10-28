package codes.spectrum.logging.slf4j.interception

import codes.spectrum.logging.LoggerMessage
import org.slf4j.Logger
import org.slf4j.event.Level

fun catchLogMessages(innerLogger: Logger? = null, minLevel: Level? = null, failSafe: Boolean = true, body: Logger.() -> Unit): List<LoggerMessage> {
    val logger = InterceptionLogger(innerLogger, minLevel)
    try {
        logger.body()
    } catch (e: Throwable) {
        if (!failSafe) {
            throw e
        }
    }
    return logger.messages
}

fun catchLogMessageOrNull(innerLogger: Logger? = null, minLevel: Level? = null, failSafe: Boolean = true, body: Logger.() -> Unit): LoggerMessage? = catchLogMessages(innerLogger, minLevel, failSafe, body).firstOrNull()

fun catchLogMessage(innerLogger: Logger? = null, minLevel: Level? = null, failSafe: Boolean = true, body: Logger.() -> Unit): LoggerMessage = catchLogMessageOrNull(innerLogger, minLevel, failSafe, body)!!