package codes.spectrum.logging.slf4j

import codes.spectrum.logging.LoggerCall
import codes.spectrum.logging.slf4j.interception.InterceptionLogger
import codes.spectrum.logging.slf4j.interception.catchLogMessage
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.Logger
import org.slf4j.event.Level


internal class UnifiedLevelExtensionsTest : StringSpec({
    /**
     * Tests for isEnabled all combinations
     */
    for (l in Level.values()) {
        for (c in Level.values()) {
            val isEnabled = c.toInt() >= l.toInt()
            "isEnabled for Level.${c} if Level.${l} and LogLevel.${l} should be $isEnabled "{
                //cast to interface - force to use extension
                val logger: Logger = InterceptionLogger(level = l)
                logger.isEnabled(c) shouldBe isEnabled
            }
        }
    }

    /**
     * Tests for strings messages in all levels
     */

    for (l in Level.values()) {
        "string sygnature Level.${l}"{
            with(catchLogMessage { log(l, "Hello") }) {
                message shouldBe "Hello"
                level shouldBe l
            }
        }

        "any - simple sygnature Level.${l}"{
            with(catchLogMessage { log(l, 223) }) {
                message shouldBe "223"
                level shouldBe l
            }
        }


        "preparedCall - message only Level.${l}"{
            with(catchLogMessage { log(l, LoggerCall(message = "Hello")) }) {
                message shouldBe "Hello"
                level shouldBe l
            }
        }

        "preparedCall - message and args only Level.${l}"{
            with(catchLogMessage { log(l, LoggerCall(message = "Hello", args = listOf(1, 2))) }) {
                message shouldBe "Hello"
                args!! shouldContainExactly listOf(1, 2)
                level shouldBe l
            }
        }

        "preparedCall -  error Level.${l}"{
            val e = Exception("X")
            with(catchLogMessage { log(l, LoggerCall(error = e)) }) {
                message shouldBe "java.lang.Exception - X"
                error shouldBe e
            }
        }

        "string-arg sygnature Level.${l} and filled"{
            //cast to interface - force to use extension
            with(catchLogMessage { log(l, "Hello", 1) }) {
                args!! shouldContainExactly listOf(1)
                message shouldBe "Hello"
                level shouldBe l
            }
        }
        "string-arg1-arg2 sygnature Level.${l} and filled"{
            //cast to interface - force to use extension
            with(catchLogMessage { log(l, "Hello", 1, 2) }) {
                args!! shouldContainExactly listOf(1, 2)
                message shouldBe "Hello"
                level shouldBe l
            }
        }
        "string-vararg sygnature Level.${l} and filled"{
            //cast to interface - force to use extension
            with(catchLogMessage { log(l, "Hello", 1, 2, 3, 4) }) {
                args!! shouldContainExactly listOf(1, 2, 3, 4)
                message shouldBe "Hello"
                level shouldBe l
            }
        }

        "marker usage catched in LoggerMessage Level.${l}"{
            with(catchLogMessage { log(l, LoggerCall(marker = "test")) }) {
                marker shouldBe "test"
            }
        }

        "write errors in Level.${l}"{
            val e = Exception("hello")
            with(catchLogMessage { log(l, e) }) {
                error shouldBe e
                message shouldBe "${e.javaClass.name} - hello"
            }
        }
    }
})