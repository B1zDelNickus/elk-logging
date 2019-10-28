package codes.spectrum.logging.logback

import codes.spectrum.logging.LoggingObject
import codes.spectrum.serialization.json.Json
import codes.spectrum.serialization.json.serializers.ThrowableDescriptor
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.lang.Exception


class RabbitAppenderTest :StringSpec({
  val config = """
<configuration debug="true"
               xmlns="http://ch.qos.logback/xml/ns/logback"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://ch.qos.logback/xml/ns/logback https://raw.githubusercontent.com/enricopulatzo/logback-XSD/master/src/main/xsd/logback.xsd"
>
  <property scope="context" name="service" value="the-service"/>
  <property scope="context" name="host" value="THE-HOST"/>


      <appender name="amqp" class="org.springframework.amqp.rabbit.logback.AmqpAppender">
        <filter class="codes.spectrum.logging.logback.SetupUnifiedMDCFilter" />
        <encoder class="codes.spectrum.logging.logback.UnifiedJsonEncoder" />
        <host>${'$'}{RABBITMQ_HOST:-127.0.0.1}</host>
        <port>5672</port>
        <virtualHost></virtualHost>
        <username>guest</username>
        <password>guest</password>
        <exchangeType>topic</exchangeType>
        <exchangeName>RabbitAppenderTest</exchangeName>
        <applicationId>RabbitAppenderTest</applicationId>
        <routingKeyPattern>$MDC_RABBIT_ROUTING_PATTERN</routingKeyPattern>
        <contentType>text/plain</contentType>
        <maxSenderRetries>2</maxSenderRetries>
        <charset>UTF-8</charset>
      </appender>


  <root level="NONE">

  </root>

  <logger name="codes.spectrum" level="TRACE">
        <appender-ref ref="amqp"/>
  </logger>
</configuration>
  """

    "can write logs to rabbit"{
        logbackReconfigure(config)
        delay(200)
        logger.trace(Json.stringify(LoggingObject("sess-1",thread = "MY THREAD")))
        delay(200)
        logger.debug(Json.stringify(LoggingObject("sess-1", operation = "op-1", userId = "ivanov")))
        delay(200)
        logger.info(Json.stringify(LoggingObject("sess-1","user1")))
        delay(200)
        logger.warn(Json.stringify(LoggingObject("sess-1")))
        delay(200)
        logger.error(Json.stringify(LoggingObject("sess-1")))

        val messages = (1..5).map{ template.receive(100L)}.toList()
        messages.size shouldBe 5
        messages.forEach {
            println(it.messageProperties.receivedRoutingKey)
            it shouldNotBe  null
        }
        messages.any{it.messageProperties.receivedRoutingKey=="log.DEBUG.the-service.op-1.INIT.CREATED.ivanov.sess-1.RabbitAppenderTest.THE-HOST"} shouldBe true
        val errorMesage = template.receive(queueErrors.name)!!
        errorMesage.messageProperties.receivedRoutingKey shouldBe "log.ERROR.the-service.none.INIT.CREATED.none.sess-1.RabbitAppenderTest.THE-HOST"
    }

    "all messages are treated as json an completed with metadata"{
        logbackReconfigure(config)
        logger.trace("hello world",Exception("x",  Exception("y")))
        delay(1000)
        val message = template.receive(200L)!!
        val body = Json.read<LoggingObject>( message.body.toString(Charsets.UTF_8))
        body.message shouldBe "hello world"
        body.logger shouldBe "codes.spectrum.logging.logback.RabbitAppenderTest"
        body.timestamp!! shouldNotBe 0
        body.level shouldBe Level.TRACE
        body.serviceName shouldBe "the-service"
        body.hostName shouldBe  "THE-HOST"
        (body.error!! as ThrowableDescriptor).let {
            it.type shouldBe Exception::class.java.name
            it.message shouldBe "x"
            it.cause!!.type shouldBe Exception::class.java.name
            it.cause!!.message shouldBe "y"
        }

    }

}){
    companion object{
        val logger = LoggerFactory.getLogger(RabbitAppenderTest::class.java)
        val name = "RabbitAppenderTest"
        val factory = CachingConnectionFactory().apply {
            this.host = System.getenv("RABBITMQ_HOST")?:"127.0.0.1"
            this.port = 5672
            this.username = "guest"
            this.setPassword( "guest")
        }
        val admin = RabbitAdmin(factory)
        val exchange = ExchangeBuilder.topicExchange(name).build()
        val queue = QueueBuilder.nonDurable(name).build()
        val queueErrors = QueueBuilder.nonDurable(name+"Errors").build()
        val binding = BindingBuilder.bind(queue).to(exchange).with("#").noargs()
        val bindingErrors = BindingBuilder.bind(queueErrors).to(exchange).with("log.ERROR.#").noargs()
        val template = RabbitTemplate(factory).apply {
            this.setDefaultReceiveQueue(name)
        }
    }


    override fun beforeSpec(spec: Spec) {
        try {
            admin.deleteExchange(name)

        }catch(e:Throwable){

        }
        try{
            admin.deleteQueue(name)
        }catch(e:Throwable){

        }
        try{
            admin.deleteQueue(name+"Errors")
        }catch(e:Throwable){

        }

        admin.declareExchange(exchange)
        admin.declareQueue(queue)
        admin.declareQueue(queueErrors)
        admin.declareBinding(binding)
        admin.declareBinding(bindingErrors)
    }

    override fun afterSpec(spec: Spec) {

        logbackReconfigureToDefault()
    }
}