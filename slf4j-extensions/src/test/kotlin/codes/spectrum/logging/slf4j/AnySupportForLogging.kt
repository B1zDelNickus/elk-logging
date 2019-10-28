package codes.spectrum.logging.slf4j

import codes.spectrum.logging.slf4j.interception.catchLogMessage
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

internal class AnySupportForLogging : StringSpec({
    "process structured objects as JSON"{
        catchLogMessage { log(SampleClass()) }.message shouldBe """{
  "x": 1,
  "y": "x"
}"""
    }
}) {
    data class SampleClass(var x: Int = 1, var y: String = "x")
}