package codes.spectrum.logging.logback

import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import java.io.File

internal class ReconfigureUtilsTest : StringSpec({

    val errorLevelConfig = """<configuration>
  <root level="ERROR"></root>
</configuration>"""

    val infoLevelConfig = """<configuration>
  <root level="INFO"></root>
</configuration>"""

    val externalConfig = """<configuration>
  <include file="../build/tmp/logback.xml" optional="true"/>
</configuration>"""


    val extFile = File("../build/tmp/logback.xml")
    val extFileContent = """
        <included scan="true" scanPeriod="2 seconds">
  <root level="TRACE"></root>
</included>
    """


    val logger = LoggerFactory.getLogger("codes.spectrum.test")

    "can reconfigure at runtime"{
        logger.isInfoEnabled shouldBe true
        logbackReconfigure(errorLevelConfig)
        logger.isInfoEnabled shouldBe false
        logger.isErrorEnabled shouldBe true
        logbackReconfigure(infoLevelConfig)
        logger.isInfoEnabled shouldBe true

    }

    "can reset to default config at runtime"{
        logger.isInfoEnabled shouldBe true
        logbackReconfigure(errorLevelConfig)
        logger.isInfoEnabled shouldBe false
        logbackReconfigureToDefault()
        logger.isInfoEnabled shouldBe true
    }

    "can use external file"{
        if (extFile.exists()) {
            extFile.delete()
        }
        logbackReconfigure(externalConfig)
        logger.isTraceEnabled shouldBe false
        extFile.parentFile.mkdirs()
        extFile.writeText(extFileContent)
        logbackReconfigure(externalConfig)
        logger.isTraceEnabled shouldBe true
    }

}) {
    override fun beforeTest(testCase: TestCase) {
        logbackReconfigureToDefault()
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        logbackReconfigureToDefault()
    }
}

