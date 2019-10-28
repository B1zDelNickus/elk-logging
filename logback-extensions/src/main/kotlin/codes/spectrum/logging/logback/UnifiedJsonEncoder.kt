package codes.spectrum.logging.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.core.encoder.EncoderBase
import codes.spectrum.serialization.json.*
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.*


class UnifiedJsonEncoder<E> : EncoderBase<E>() {
    override fun headerBytes(): ByteArray  = ByteArray(0)

    override fun footerBytes(): ByteArray = ByteArray(0)

    private fun ILoggingEvent.prop(name:String):String? {
        return this.loggerContextVO.propertyMap[name]
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").apply{
		timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun encode(event: E): ByteArray {
        val e = event as ILoggingEvent
        val message = e.message
        var json = if(message.startsWith("{")){
            Json.read(message,JsonObject::class.java)
        }else{
            JsonObject().apply {
                this.set("message",message)
            }
        }
        json.setDefault("hostName",{event.prop("host")?:java.net.InetAddress.getLocalHost().hostName})
        val timestamp = json.getOrNull("timestamp")?.asString?.let{dateFormat.parse(it)} ?: Date(event.timeStamp)
        json.remove("timestamp")
        json.set("@timestamp", dateFormat.format(timestamp))
        json.setDefault("serviceName",{event.prop("service")?:System.getenv("SERVICE_NAME")?:"no-service"})
        json.setDefault("level",{event.level.toString()})
        json.setDefault("logger",{event.loggerName})
        json.setDefault("timeFromStart",{event.timeStamp - event.loggerContextVO.birthTime})
        json.setDefault("thread",{event.threadName})
        if(null!=event.throwableProxy){
            json.setDefault("error",Json.jsonify((event.throwableProxy as ThrowableProxy).throwable))
        }
        if(null!=event.marker) {
            json.setDefault("marker", { event.marker!!.toString()})
        }
        return Json.stringify(json).toByteArray(Charsets.UTF_8)
    }

}