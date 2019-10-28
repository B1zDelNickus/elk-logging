package codes.spectrum.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxy
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*



val error: Throwable = try {
    {
        {
            {
                {
                    {
                        {
                            {
                                {
                                    {
                                        {
                                            throw java.lang.Exception("test")
                                        }()
                                    }()
                                }()
                            }()
                        }()
                    }()
                }()
            }()
        }()
    }()
} catch (e: Throwable) {
    e
}

internal class DefaultStdOutLayoutTest : StringSpec({

    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    "overall test - single line in message, constrainted stack trace, relative time, thread"{
        result shouldBe """${formatter.format(Date(default_event.timeStamp))} 100 INFO  sample-host [thread-1] sample-service some.logger - multi string with spaces
java.lang.Exception: test
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt:27)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt:26)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt:25)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt:24)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt:23)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt${'$'}error${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1${'$'}1.invoke(DefaultStdOutLayoutTest.kt)"""
            .trim().replace("""[\r\n]+""".toRegex(), "\n")
        println(result)
    }

    "can parse header with default regex"{
        val result = DEFAULT_STD_OUT_PATTERN_REGEX.matchEntire("""2019-01-01T23:02:03.222+0500 100 INFO  sample-host [thread-1] sample-service some.logger - multi string with spaces""")!!
        var count:Int = 1
        fun check(value:String, number:Int = count++) {
            result.groupValues[number] shouldBe value
        }
        check("2019-01-01T23:02:03.222+0500")
        check("100")
        check("INFO")
        check("sample-host")
        check("thread-1")
        check("sample-service")
        check("some.logger")
        check("multi string with spaces")
    }
    "can parse with stack with default regex"{
        val result = DEFAULT_STD_OUT_PATTERN_REGEX.matchEntire("""2019-01-01T23:02:03.222+0500 100 INFO  sample-host [thread-1] sample-service some.logger - multi string with spaces
java.lang.Exception: test
	at codes.spectrum.logging.logback.2
	at codes.spectrum.logging.logback.1""")!!
        var count:Int = 1
        fun check(value:String, number:Int = count++) {
            result.groupValues[number] shouldBe value
        }
        check("2019-01-01T23:02:03.222+0500")
        check("100")
        check("INFO")
        check("sample-host")
        check("thread-1")
        check("sample-service")
        check("some.logger")
        check("multi string with spaces")
        count++
        check("java.lang.Exception")
        check("test")
        check("""
	at codes.spectrum.logging.logback.2
	at codes.spectrum.logging.logback.1""")
    }
}) {
    companion object {

        val default_context = (LoggerFactory.getILoggerFactory() as LoggerContext).also {
            it.putProperty("service", "sample-service")
            it.putProperty("host", "sample-host")
        }
        val birth_time = default_context.loggerContextRemoteView.birthTime
        val layout = PatternLayout().apply {
            pattern = DEFAULT_STD_OUT_PATTERN
            this.context = default_context
        }.also {
            it.start()
        }
        val default_event = LoggingEvent().apply {
            message = "multi\n\r\tstring   with    spaces"
            level = Level.INFO
            loggerName = "some.logger"
            timeStamp = birth_time + 100
            threadName = "thread-1"
            setThrowableProxy(ThrowableProxy(error))
            this.setLoggerContextRemoteView(default_context.loggerContextRemoteView)
        }
        val result get() = layout.doLayout(default_event).trim().replace("""[\r\n]+""".toRegex(), "\n")
    }


}