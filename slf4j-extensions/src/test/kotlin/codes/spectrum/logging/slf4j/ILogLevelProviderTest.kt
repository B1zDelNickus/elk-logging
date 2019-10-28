package codes.spectrum.logging.slf4j

import codes.spectrum.logging.ILogLevelProvider
import codes.spectrum.logging.slf4j.interception.catchLogMessage
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.event.Level


internal class ILogLevelProviderTest : StringSpec({
    for (l in Level.values()) {
        "send any with ILogLevelProvider for Level.${l}"{
            catchLogMessage { log(WithLogLevel(l)) }.level shouldBe l
        }
    }
    "exceptions are error level by default"{
        catchLogMessage { log(Exception()) }.level shouldBe Level.ERROR
    }
    "exceptions with ILogLevelProvider are dynamic-level"{
        catchLogMessage { log(WithLogLevelException()) }.level shouldBe Level.WARN
    }
}) {
    class WithLogLevel(val level: Level) : ILogLevelProvider {
        override fun getLogLevel(): Level = level
    }

    class WithLogLevelException : Exception(""), ILogLevelProvider {
        override fun getLogLevel(): Level = Level.WARN
    }

}