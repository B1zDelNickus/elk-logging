package codes.spectrum.logging.logback

import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec

class VisibilityOfClassesTest :StringSpec({
    "can see AmqpAppender"{
        Class.forName("org.springframework.amqp.rabbit.logback.AmqpAppender") shouldNotBe null
    }
})