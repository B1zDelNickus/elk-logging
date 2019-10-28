package codes.spectrum.logging

import codes.spectrum.logging.slf4j.interception.catchLogMessages
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.*


class DefaultLoggingTest : StringSpec({
    "no-hand-mode "{
        catchLogMessages {
            logging(this) {
                subOperation("delay-10") {
                    Thread.sleep(10)
                }
                subOperation("calculation") {
                    val x = 1 * 1230
                }
            }
        }.size shouldBe 6
    }
    /*
2019-06-17T15:55:13.892+0500 748 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.logging.DefaultLoggingTest - { "phase": "INIT", "state": "CREATED", "count": 1, "created": "2019-06-17T15:55:13+0500", "duration": 0 }
2019-06-17T15:55:13.895+0500 751 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.logging.DefaultLoggingTest - { "operation": "delay-10", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:55:13+0500", "duration": 0 }
2019-06-17T15:55:13.907+0500 763 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.logging.DefaultLoggingTest - { "operation": "delay-10", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:55:13+0500", "duration": 12, "finished": "2019-06-17T15:55:13+0500" }
2019-06-17T15:55:13.908+0500 764 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.logging.DefaultLoggingTest - { "operation": "calculation", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:55:13+0500", "duration": 0 }
2019-06-17T15:55:13.908+0500 764 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.logging.DefaultLoggingTest - { "operation": "calculation", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:55:13+0500", "duration": 0, "finished": "2019-06-17T15:55:13+0500" }
2019-06-17T15:55:13.909+0500 765 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.logging.DefaultLoggingTest - { "phase": "COMPLETE", "state": "ERROR", "count": 1, "created": "2019-06-17T15:55:13+0500", "duration": 102, "finished": "2019-06-17T15:55:13+0500", "counters": { "duration-delay-10": 12, "count-delay-10": 1, "count-calculation": 1 } }
     */

    "operation stack and sessions"{
        catchLogMessages {
            // this is Logger
            logging(this, "work", setup = {
                this.logobj.sessionId = Date().time.toString()
                this.logobj.requestId = "123"
            }) {
                subOperation("delay-10") {
                    Thread.sleep(10)
                }
                subOperation("calculation") {
                    val x = 1 * 1230
                }
            }
        }.size shouldBe 6
    }
/*
2019-06-17T15:58:11.301+0500 765 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769091300", "requestId": "123", "operation": "work", "phase": "INIT", "state": "CREATED", "count": 1, "created": "2019-06-17T15:58:11+0500", "duration": 0 }
2019-06-17T15:58:11.304+0500 768 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769091300", "requestId": "123", "operation": "work/delay-10", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:58:11+0500", "duration": 0 }
2019-06-17T15:58:11.316+0500 780 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769091300", "requestId": "123", "operation": "work/delay-10", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:58:11+0500", "duration": 12, "finished": "2019-06-17T15:58:11+0500" }
2019-06-17T15:58:11.317+0500 781 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769091300", "requestId": "123", "operation": "work/calculation", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:58:11+0500", "duration": 0 }
2019-06-17T15:58:11.317+0500 781 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769091300", "requestId": "123", "operation": "work/calculation", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:58:11+0500", "duration": 1, "finished": "2019-06-17T15:58:11+0500" }
2019-06-17T15:58:11.317+0500 781 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769091300", "requestId": "123", "operation": "work", "phase": "COMPLETE", "state": "ERROR", "count": 1, "created": "2019-06-17T15:58:11+0500", "duration": 17, "finished": "2019-06-17T15:58:11+0500", "counters": { "duration-delay-10": 12, "count-delay-10": 1, "duration-calculation": 1, "count-calculation": 1 } }
 */
    "own counters and log calls"{
        catchLogMessages {
            logging(this, "work", setup = {
                this.logobj.sessionId = Date().time.toString()
                this.logobj.requestId = "123"
            }) {
                subOperation("delay-10") {
                    Thread.sleep(10)
                    logobj.add("sleeped", 10)
                    logobj.parent?.inc("sleeped-count")
                    log()
                }
                subOperation("calculation") {
                    val x = 1 * 1230
                }
                subOperation("delay-20") {
                    Thread.sleep(20)
                    logobj.add("sleeped", 20)
                    logobj.parent?.inc("sleeped-count")
                    log()
                }
            }
        }.size shouldBe 10
    }
    /*
2019-06-17T15:59:00.069+0500 789 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work", "phase": "INIT", "state": "CREATED", "count": 1, "created": "2019-06-17T15:59:00+0500", "duration": 0 }
2019-06-17T15:59:00.072+0500 792 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/delay-10", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 0 }
2019-06-17T15:59:00.082+0500 802 TRACE DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/delay-10", "phase": "EXECUTE", "state": "PROGRESS", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 10, "counters": { "sleeped": 10 } }
2019-06-17T15:59:00.082+0500 802 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/delay-10", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 10, "finished": "2019-06-17T15:59:00+0500", "counters": { "sleeped": 10 } }
2019-06-17T15:59:00.083+0500 803 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/calculation", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 0 }
2019-06-17T15:59:00.083+0500 803 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/calculation", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 0, "finished": "2019-06-17T15:59:00+0500" }
2019-06-17T15:59:00.084+0500 804 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/delay-20", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 0 }
2019-06-17T15:59:00.105+0500 825 TRACE DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/delay-20", "phase": "EXECUTE", "state": "PROGRESS", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 21, "counters": { "sleeped": 20 } }
2019-06-17T15:59:00.105+0500 825 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work/delay-20", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:59:00+0500", "duration": 21, "finished": "2019-06-17T15:59:00+0500", "counters": { "sleeped": 20 } }
2019-06-17T15:59:00.106+0500 826 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#12] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "1560769140068", "requestId": "123", "operation": "work", "phase": "COMPLETE", "state": "ERROR", "count": 1, "created": "2019-06-17T15:59:00+0500", "duration": 37, "finished": "2019-06-17T15:59:00+0500", "counters": { "sleeped-count": 2, "duration-delay-20": 21, "duration-delay-10": 10, "count-delay-10": 1, "count-delay-20": 1, "count-calculation": 1 } }
     */




    "with some logobj values and state usage"{
        catchLogMessages {
            logging(this, setup = {
                this.logobj.sessionId = "123"
            }) {
                for (i in 1..3) {
                    logobj.inc("test")
                    log()
                }
                subOperation("calling-rest") {
                    this.logobj.values["rest-result"] = 2
                    log()
                }
                logobj.values["result"] = "Hello"
                logobj.state = "RESULT"
                debug()
            }
        }.size shouldBe 9
        // IN STD LOOCKS LIKE
        /*
2019-06-17T15:54:03.800+0500 830 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "phase": "INIT", "state": "CREATED", "count": 1, "created": "2019-06-17T15:54:03+0500", "duration": 0 }
2019-06-17T15:54:03.802+0500 832 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "created": "2019-06-17T15:54:03+0500", "duration": 3, "counters": { "test": 1 } }
2019-06-17T15:54:03.803+0500 833 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "created": "2019-06-17T15:54:03+0500", "duration": 4, "counters": { "test": 2 } }
2019-06-17T15:54:03.803+0500 833 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "created": "2019-06-17T15:54:03+0500", "duration": 4, "counters": { "test": 3 } }
2019-06-17T15:54:03.804+0500 834 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "operation": "calling-rest", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-17T15:54:03+0500", "duration": 0 }
2019-06-17T15:54:03.807+0500 837 TRACE DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "operation": "calling-rest", "phase": "EXECUTE", "state": "PROGRESS", "count": 0, "created": "2019-06-17T15:54:03+0500", "duration": 1, "values": { "rest-result": 2 } }
2019-06-17T15:54:03.807+0500 837 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "operation": "calling-rest", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-17T15:54:03+0500", "duration": 4, "finished": "2019-06-17T15:54:03+0500", "values": { "rest-result": 2 } }
2019-06-17T15:54:03.808+0500 838 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "phase": "EXECUTE", "state": "RESULT", "count": 1, "created": "2019-06-17T15:54:03+0500", "duration": 4, "values": { "result": "Hello" }, "counters": { "test": 3, "duration-calling-rest": 4, "count-calling-rest": 1 } }
2019-06-17T15:54:03.808+0500 838 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#16] no-service c.s.logging.DefaultLoggingTest - { "sessionId": "123", "phase": "COMPLETE", "state": "ERROR", "count": 1, "created": "2019-06-17T15:54:03+0500", "duration": 9, "finished": "2019-06-17T15:54:03+0500", "values": { "result": "Hello" }, "counters": { "test": 3, "duration-calling-rest": 4, "count-calling-rest": 1 } }

         */
    }
    
    "with custom levels" {
        catchLogMessages {
            logging(this, setup = {
                this.logobj.sessionId = "123"
                this.startLevel = Level.DEBUG
                this.finishLevel = Level.DEBUG
                this.bodyLevel = Level.TRACE
            }) {
                for (i in 1..3) {
                    logobj.inc("test")
                    log()
                }
                info()
                subOperation("calling-rest", setup = {
                    this.startLevel = Level.INFO
                    this.finishLevel = Level.TRACE
                }) {
                    this.logobj.values["rest-result"] = 404
                    log()
                }
                logobj.values["result"] = "Hello"
                logobj.state = "RESULT"
            }
        }.size shouldBe 9
        /*
2019-06-24T17:36:16.042+0500 1092 DEBUG DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#32] no-service codes.spectrum - { "sessionId": "123", "phase": "INIT", "state": "CREATED", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.041+0000", "duration": 0, "level": "DEBUG" }
2019-06-24T17:36:16.046+0500 1096 TRACE DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#33] no-service codes.spectrum - { "sessionId": "123", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.041+0000", "duration": 5, "counters": { "test": 1 }, "level": "DEBUG" }
2019-06-24T17:36:16.047+0500 1097 TRACE DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#33] no-service codes.spectrum - { "sessionId": "123", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.041+0000", "duration": 6, "counters": { "test": 2 }, "level": "DEBUG" }
2019-06-24T17:36:16.048+0500 1098 TRACE DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#33] no-service codes.spectrum - { "sessionId": "123", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.041+0000", "duration": 7, "counters": { "test": 3 }, "level": "DEBUG" }
2019-06-24T17:36:16.048+0500 1098 INFO  DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#33] no-service codes.spectrum - { "sessionId": "123", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.041+0000", "duration": 7, "counters": { "test": 3 }, "level": "DEBUG" }
2019-06-24T17:36:16.051+0500 1101 INFO  DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#33] no-service codes.spectrum - { "sessionId": "123", "operation": "calling-rest", "phase": "INIT", "state": "CREATED", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.051+0000", "duration": 0, "level": "DEBUG" }
2019-06-24T17:36:16.052+0500 1102 TRACE DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#34] no-service codes.spectrum - { "sessionId": "123", "operation": "calling-rest", "phase": "EXECUTE", "state": "PROGRESS", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.051+0000", "duration": 0, "values": { "rest-result": 404 }, "level": "DEBUG" }
2019-06-24T17:36:16.052+0500 1102 TRACE DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#33] no-service codes.spectrum - { "sessionId": "123", "operation": "calling-rest", "phase": "COMPLETE", "state": "COMPLETE", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.051+0000", "duration": 1, "finished": "2019-06-24T12:36:16.052+0000", "values": { "rest-result": 404 }, "level": "DEBUG" }
2019-06-24T17:36:16.052+0500 1102 DEBUG DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#32] no-service codes.spectrum - { "sessionId": "123", "phase": "COMPLETE", "state": "COMPLETE", "count": 1, "error_count": 0, "created": "2019-06-24T12:36:16.041+0000", "duration": 11, "finished": "2019-06-24T12:36:16.052+0000", "values": { "result": "Hello" }, "counters": { "test": 3, "duration-calling-rest": 1, "count-calling-rest": 1 }, "level": "DEBUG" }
         */
    }
})