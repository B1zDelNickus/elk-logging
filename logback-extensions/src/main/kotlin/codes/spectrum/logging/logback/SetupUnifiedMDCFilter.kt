package codes.spectrum.logging.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import codes.spectrum.logging.LoggingObject
import codes.spectrum.serialization.json.Json
import org.slf4j.MDC

class SetupUnifiedMDCFilter<E>: Filter<E>(){
    private fun ILoggingEvent.prop(name:String):String? {
        return this.loggerContextVO.propertyMap[name]
    }
    override fun decide(event: E): FilterReply {
        val e = event as ILoggingEvent
        val message = e.message
        if(message.startsWith("{")) {
            val loggerObject = Json.read<LoggingObject>(message)
            MDC.put("operation", (loggerObject.operation ?: "none").replace("/", "--"))
            MDC.put("session", loggerObject.sessionId ?: "none")
            MDC.put("user", loggerObject.userId ?: "none")
            MDC.put("phase", loggerObject.phase ?: "none")
            MDC.put("state", loggerObject.state ?: "none")
            MDC.put("task", loggerObject.taskId ?: "none")
            MDC.put("request", loggerObject.requestId ?: "none")
            MDC.put("logger-tail", e.loggerName.split(".").last())
        }else{
            MDC.put("operation", "none")
            MDC.put("session", "none")
            MDC.put("user", "none")
            MDC.put("phase", "none")
            MDC.put("state", "none")
            MDC.put("task", "none")
            MDC.put("request", "none")
            MDC.put("logger-tail", e.loggerName.split(".").last())
        }
        return FilterReply.NEUTRAL
    }

}