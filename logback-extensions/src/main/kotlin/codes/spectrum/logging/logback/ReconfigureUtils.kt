package codes.spectrum.logging.logback

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream


fun logbackReconfigure(file: File) = logbackReconfigure(file.inputStream())
fun logbackReconfigure(content: String) = logbackReconfigure(content.byteInputStream())
fun logbackReconfigure(reader: InputStream) {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val configurator = JoranConfigurator()
    configurator.context = context
    context.reset()
    configurator.doConfigure(reader);
}

fun logbackReconfigureToDefault() {
    val classloader = ClassLoader.getSystemClassLoader()
    for (resource in arrayOf("test-logback.xml", "logback.xml")) {
        if (null != classloader.getResource(resource)) {
            logbackReconfigure(classloader.getResourceAsStream(resource))
            return
        }
    }
}