package codes.spectrum.logging.slf4j

import codes.spectrum.logging.slf4j.interception.catchLogMessage
import codes.spectrum.logging.slf4j.interception.catchLogMessageOrNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.event.Level

internal class LazyLoggingTest : StringSpec({
    "will be called if level match"{
        var called = false
        catchLogMessage {
            log { called = true;"called" }
        }.message shouldBe "called"
        called shouldBe true

    }

    "will not be called if level not match"{
        var called = false
        catchLogMessageOrNull(minLevel = Level.ERROR) {
            log { called = true;"called" }
        } shouldBe null
        called shouldBe false
    }
})