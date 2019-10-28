# Общие классы для поддержки логирования Spectrum

## Структура проекта

1. `commons` - библиотека с общими классами поддержки журналов
2. `slf4j-extensions` - расширения на SLF4J для удобной работы с SLF
3. `logback-extensions` - расширения для LOGBACK
4. `templates` - идиомы использования логирования в проектах, обвязки вокруг вызовов
4. `bundle` - объединенная библиотека с зависимостями и объединяющим "сахаром"


## Использование в проекте

```kotlin
//при использовании нашего buildSrc
dependencies {
   spectrumLogging()
}
```

```kotlin
// напрямую
dependencies {
   compile("codes.spectrum.logging:spectrum-logging-bundle:0.5-dev-SNAPSHOT")
}
```

Если требуется использование RABBITMQ, то дополнительно:
```kotlin
//при использовании нашего buildSrc
dependencies {
   spectrumLogging()
   rabbitMqAppender()
}
```

```kotlin
// напрямую
dependencies {
   compile("codes.spectrum.logging:spectrum-logging-bundle:0.5-dev-SNAPSHOT")
   compile("org.springframework.amqp:spring-rabbit:2.1.7.RELEASE")
}
```
> В целом `buildSrc` уже автоматически добавляет зависимости от spectrumLogging в обычные 
библиотеки и rabbitMqAppender в докеры

## Упрощение работы с SLF логерами

### Поддержка указания уровня логирования как параметра

Какой код мотивирует такое расширение

```kotlin
//есть некая run-time настройка
val writeDetailLogLevel = Level.valueOf(GlobalConfig.get("DETAIL_LOG_LEVEL","INFO"))
fun doSomething(){
  when(writeDetailLogLevel){
     Level.DEBUG -> logger.debug("Start")
     // 4 таких строки
  }
  // что-то делается
  when(writeDetailLogLevel){
       Level.DEBUG -> logger.debug("Finish")
       // 4 таких строки
    }
}
```
В итоге это либо лапша, либо отказ от такой возможности либо что-то недоделанное

Все сигнатуры SLF Logger из семейства вида `Logger.debug(...), trace(...)` и т.д.
дополннены унифицированным вызовом `Logger.log(level:Level,...)`,
который сам затем вызывает нужный метод SLF.

Также реализован унифицированный вызов `Logger.isEnabled(level:Level)` в том числе 
и для маркеров (Marker) и в виде `Logger.isEnabled(level:Level, marker:Marker)`,
так и в сигнатуре `Logger.isEnabled(level:Level, marker:String)`

Теперь код можно переписывать так:


```kotlin
//есть некая run-time настройка
val writeDetailLogLevel = Level.valueOf(GlobalConfig.get("DETAIL_LOG_LEVEL","INFO"))
fun doSomething(){
  logger.log(writeDetailLogLevel, "Start")
  //
  logger.log(writeDetailLogLevel, "Finish")
}
```

### Поддержка логирования произвольного объекта

Помимо штатных сигнатур с `message:String` добавлены сигнатуры
с `message:Any`. Которые обрабатываются следующим образом:
1. `String -> String`  (без изменений)
1. `LoggerCall -> LoggerCall`  (без изменений - это специальный класс обертка для вызовов SLF)
1. `(()->Any?) -> Any? -> Any - recursion` (ламбды вызываются и дальше объект полученный из нее уже обрабатывается)
1. `Throwable -> (message:it.type+it.message)+it` (формируется обычный логинг ошибки с сообщением из типа ошибки и сообщения) 
1. `Any -> Json.stringify(it, format=false)` - в остальных случаях пакуется в однострочный JSON

> Внимание! Если передается просто Throwable - он обрабатывается именно по этой логике, типовой для SLF
если же нужно получить ошибку в виде JSON - ее надо или сразу передавать как `Json.stringify(error)` или
паковать в тот или иной класс

### Автоматическое определение уровня логирования

Также поддерживается вызов `Logger.log(message:Any)` с автоматическим применением уровня по следующей логике
1. `ILogLevelProvider -> it.getLogLevel()`
1. `Throwable -> ERROR`
1. `Any -> INFO`

### Ленивое логирование

Типовой код выглядит так:
```kotlin
val logObject = doCreateLogObjectLongTime() // мы создаем нечто, что хотим отлогировать
logger.trace(logObject.toString()) // и логируем 
```
Причем чем больше нужно собирать в этот объект подробностей - тем чаще это еще и какой-то отладочный `trace`
Чуть получше когда пишется так:
```kotlin
if(logger.isTraceEnabled()){
    val logObject = doCreateLogObjectLongTime() // мы создаем нечто, что хотим отлогировать
    logger.trace(logObject.toString()) // и логируем 
}
```
Но это выглядит не по-котлински.
В расширения добавлены сигнатуры для явного употребления лямбд для лениового выполнения, например данный пример можно переписать
как
```kotlin
logger.trace{doCreateLogObjectLongTime()}
```

### Перехват и буферизация сообщений

Прежде всего это применимо в тестах на запись лога.
Добавлен специальный логер `codes.spectrum.logging.slf4j.interception.InterceptionLogger`, который может использоваться
прежде всего при тестировании.

Ниже приведен заголовок конструктора класса для пояснений того с какими параметрами создается

```kotlin

/**
 * Реализация SLF Logger для записи объектов логинга в память
 * может использовать для проксирования другого целевого логгера
 */
class InMemoryProxySlfLogger(
    /**
     * Целевой логгер, в который может проксироваться вызов, не обязателен
     */
    val innerLogger: Logger? = null,
    /**
     * Собственный общий минимальный уровень - если установлен - то перекрывает
     * уровень innerLogger, если сообщение не проходит этот уровень - оно не пишется
     * ни в память, не уходит и на innerLogger
     */
    val level: Level? = null,
    /**
     * Собственное имя логгера, если установлено перекрывает innerLogger
     */
    val selfName: String? = null,
    /**
     * Уровень для записи в память (может быть выше чем level, не блокирует запись в innerLog)
     * в отличие от level - никак не влияет на запись в innerLog
     */
    val memoryLevel: Level? = null,
    /**
     * Хранилище сообщений - собственно буфер из которого затем можно прочитать накопленные сообщения
     */
    val messages: MutableList<LoggerMessage> = mutableListOf()
)
```

Типовой сценарий использования не предполагает использования внутри реального innerLogger - это обычный тестовый сценарий


```kotlin
class Service(logger:Logger=LoggerFactory.getLogger(Service::class.java)){
   fun exec(){
       logger.log("Start")
   }
}
// создается 
val logger = InMemoryProxySlfLogger()
Service(logger).exec()
logger.messages[0]!!.message shouldBe "Start"
// В STD не будет ничего

```

Если логи не надо полностью перехватывать, то можно передать нужный логер


```kotlin
class Service(logger:Logger=LoggerFactory.getLogger(Service::class.java)){
   fun exec(){
       logger.log("Start")
   }
}
// создается 
val logger = InMemoryProxySlfLogger(LoggerFactory.getLogger(Service::class.java))
Service(logger).exec()
logger.messages[0]!!.message shouldBe "Start"
// в STD будут типовые сообщения
```

Также в пакет добавлены утилиты для перехвата сообщений (используется в основном для тестов)

Простые перехваты
```kotlin
// просто перехват первого сообщения
catchLogMessage{trace("X")}.message shouldBe "X"
// null - вариант
catchLogMessageOrNull{} shouldBe null
// перехват всего списка
catchLogMessages{trace("X");debug("Y")}.size shouldBe 2
// прячет ошибки (так как нас интересует что в лог записала ламбда, а не то что там ошибок не было
catchLogMessages{
   try{
      trace("X")
      throw Exception("aaaa")
   }catch(e:Throwable){
      error(e)
      throw e
   }
}.size shouldBe 2 // не будет ошибки и будут оба сообщения
```

Дополнительные опции
```kotlin
// с внутренним логгером
val logger = LoggerFactory.getLogger(MyClass::class.java)
catchLogMessage(innerLogger=logger){trace("X")}.message shouldBe "X" //+ обычный логинг

// с повышением уровня
catchLogMessages(level=Level.ERROR){trace("x");debug("Y");error("Z")}.size shouldBe 1

// без защиты от ошибок
catchLogMessages(failSafe= false){
   try{
      trace("X")
      throw Exception("aaaa")
   }catch(e:Throwable){
      error(e)
      throw e
   }
}// - не вернет результат - завалится с ошибкой

```

## Шаблонная обертка вызова в логгер

Типовой сценарий логирования
1. Начало некоей операции
2. Значимые события операции
3. Заврешение или ошибка операции

Чтобы не работать с этим в ручную добавлен шаблонный вызов.
В примере ниже показан пример сервиса, который
выполняет некую работу и при этом разбивает ее на 2 дополнительные под-операции
```kotlin
 class SomeService(
        // у сервиса должен быть логер и его лучше иметь возможность передать
        // как параметр конструктоа
        val logger: Logger = LoggerFactory.getLogger(SomeService::class.java),
        // также у сервиса должно быть имя операции, которую он выполняет и лучше бы
        // оно настраивалось
        val operationName : String = "do-cool-work"
    ){

        // некая значимая функция
        fun execute():String =
            logging(logger,operationName){ // умеет возвращать значение
                var cacheValue = ""
                subOperation("read-cache"){
                    // мы в suspend и у нас разрешены coroutines
                    delay(1000)
                    cacheValue = "123" // имитация некоей первой операции с БД
                }
                var result = ""
                subOperation("prepare-result"){
                    delay(200)
                    result = cacheValue+"-result"
                }
                result
            }
    }
//вызов
SomeClass().execute()

```

При таком вызове журнал будет содержать следующие сообщения:
```
// общий вход
2019-06-18T18:52:23.355+0500 761 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work", "phase": "INIT", "state": "CREATED", "count": 1, "created": "2019-06-18T18:52:23+0500", "duration": 0 }
// вход в первую операцию (внутрениие операции по умолчанию понижены в уровне логировангия)
2019-06-18T18:52:23.359+0500 765 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#5] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/read-cache", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-18T18:52:23+0500", "duration": 0 }
// выход из первой операции
2019-06-18T18:52:24.364+0500 1770 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#5] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/read-cache", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-18T18:52:23+0500", "duration": 1004, "finished": "2019-06-18T18:52:24+0500" }
// вход во вторую операцию
2019-06-18T18:52:24.365+0500 1771 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#5] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/prepare-result", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-18T18:52:24+0500", "duration": 0 }
// выход из второй операции
2019-06-18T18:52:24.568+0500 1974 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#5] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/prepare-result", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-18T18:52:24+0500", "duration": 203, "finished": "2019-06-18T18:52:24+0500" }
// общий выход (собрана статистика по операциям внутри)
2019-06-18T18:52:24.569+0500 1975 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#4] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work", "phase": "COMPLETE", "state": "ERROR", "count": 1, "created": "2019-06-18T18:52:23+0500", "duration": 1298, "finished": "2019-06-18T18:52:24+0500", "counters": { "count-read-cache": 1, "duration-prepare-result": 203, "duration-read-cache": 1004, "count-prepare-result": 1 } }
```

То есть не формируя НИ ОДНОГО ручного вызова журнала, не ведя никаких счетчиков и не меря duration - это все приходит из коробки

Допустим в процессе выполнния второй операции произошла ошибка,
тогда такая трасса:

```
2019-06-18T18:59:47.371+0500 738 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#6] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work", "phase": "INIT", "state": "CREATED", "count": 1, "created": "2019-06-18T18:59:47+0500", "duration": 0 }
2019-06-18T18:59:47.376+0500 743 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#7] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/read-cache", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-18T18:59:47+0500", "duration": 0 }
2019-06-18T18:59:48.380+0500 1747 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#7] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/read-cache", "phase": "COMPLETE", "state": "COMPLETE", "count": 0, "created": "2019-06-18T18:59:47+0500", "duration": 1005, "finished": "2019-06-18T18:59:48+0500" }
2019-06-18T18:59:48.381+0500 1748 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#7] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/prepare-result", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-18T18:59:48+0500", "duration": 0 }
// ошибки залогированы и на уровне подоперации
2019-06-18T18:59:48.588+0500 1955 ERROR DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#7] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/prepare-result", "phase": "COMPLETE", "state": "ERROR", "count": 0, "created": "2019-06-18T18:59:48+0500", "duration": 203, "finished": "2019-06-18T18:59:48+0500", "error": { "type": "java.lang.Exception", "message": "what the hell!!!", "stack": [ "codes.spectrum.logging.ForReadmeGenerator$SomeService$execute$1$2.invokeSuspend at ForReadmeGenerator.kt:37", "kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith at ContinuationImpl.kt:33", "codes.spectrum.logging.LoggingContext.execute at LoggingContext.kt:45", "codes.spectrum.logging.LoggingContext.subOperation at LoggingContext.kt:62", "codes.spectrum.logging.LoggingContext.subOperation$default at LoggingContext.kt:56" ] } }
// и на уровне операции, также выставлены счетчики ошибок
2019-06-18T18:59:48.589+0500 1956 ERROR DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#6] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work", "phase": "COMPLETE", "state": "ERROR", "count": 1, "created": "2019-06-18T18:59:47+0500", "duration": 1302, "finished": "2019-06-18T18:59:48+0500", "error": { "type": "java.lang.Exception", "message": "what the hell!!!", "stack": [ "codes.spectrum.logging.ForReadmeGenerator$SomeService$execute$1$2.invokeSuspend at ForReadmeGenerator.kt:37", "kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith at ContinuationImpl.kt:33", "codes.spectrum.logging.LoggingContext.execute at LoggingContext.kt:45", "codes.spectrum.logging.LoggingContext.subOperation at LoggingContext.kt:62", "codes.spectrum.logging.LoggingContext.subOperation$default at LoggingContext.kt:56" ] }, "counters": { "count-read-cache": 1, "duration-prepare-result": 203, "errors-prepare-result": 1, "duration-read-cache": 1005, "count-prepare-result": 1 } }
```

Теперь более сложный пример - собственные записи в журнал
```kotlin
class SomeService(
        // у сервиса должен быть логер и его лучше иметь возможность передать
        // как параметр конструктоа
        val logger: Logger = LoggerFactory.getLogger(SomeService::class.java),
        // также у сервиса должно быть имя операции, которую он выполняет и лучше бы
        // оно настраивалось
        val operationName : String = "do-cool-work"
    ){

        // некая значимая функция
        fun execute():String =// собственно вход в функцию
            logging(logger,operationName){
              // мы  имеем доступ к корневому контексту логера
                val rootLog = this
                var cacheValue = ""
                subOperation("read-cache",setup={
                // мы можем установить уровень записи подоперации (по умолчнию был бы DEBUG)
                    setAllLevels(Level.INFO)
                }){
                    delay(1000)
                    cacheValue = "123" // имитация некоей первой операции с БД
                    // мы можем выставить значение в свой контекст логирования
                    // logobj это часть LoggingContext, пришедшего в subOperation
                    logobj.set("cachedValue", cacheValue)
                    // вызов дефолтной записи в нужный уровень логирования нового состояния logobj
                    log()
                    // и в родительский тоже можем отправить что надо
                    rootLog.logobj.set("from-cache", true)
                    // накрутили некий счетчик db-ops на 1
                    rootLog.logobj.inc("db-ops")
                    
                }
                var result = ""
                subOperation("prepare-result"){
                    delay(200)
                    //вторая операция накрутила тот же счетчик на 20
                    rootLog.logobj.add("db-ops",20)
                    result = cacheValue+"-result"
                }
                result
            }
    }
```

На выходе трасса

```
2019-06-18T19:05:36.904+0500 761 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work", "phase": "INIT", "state": "CREATED", "count": 1, "created": "2019-06-18T19:05:36+0500", "duration": 0 }
// уровень первой операции выше - не DEBUG
2019-06-18T19:05:36.907+0500 764 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#9] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/read-cache", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-18T19:05:36+0500", "duration": 0 }
// есть собственная запись и фаза прогресс с кастомным значением
2019-06-18T19:05:37.915+0500 1772 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#10] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/read-cache", "phase": "EXECUTE", "state": "PROGRESS", "count": 0, "created": "2019-06-18T19:05:36+0500", "duration": 1004, "values": { "cachedValue": "123" } }
2019-06-18T19:05:37.916+0500 1773 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#9] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/read-cache", "phase": "COMPLETE", "state": "COMPLETE", "count": 0, "created": "2019-06-18T19:05:36+0500", "duration": 1008, "finished": "2019-06-18T19:05:37+0500", "values": { "cachedValue": "123" } }
2019-06-18T19:05:37.917+0500 1774 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#9] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/prepare-result", "phase": "INIT", "state": "CREATED", "count": 0, "created": "2019-06-18T19:05:37+0500", "duration": 0 }
2019-06-18T19:05:38.118+0500 1975 DEBUG DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#9] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work/prepare-result", "phase": "COMPLETE", "state": "COMPLETE", "count": 0, "created": "2019-06-18T19:05:37+0500", "duration": 201, "finished": "2019-06-18T19:05:38+0500" }
// в итоговой записи каунтеры и значения доставленные из под-операций
2019-06-18T19:05:38.119+0500 1976 INFO  DESKTOP-GHGCB6U [pool-2-thread-1 @coroutine#8] no-service c.s.l.ForReadmeGenerator$SomeService - { "operation": "do-cool-work", "phase": "COMPLETE", "state": "COMPLETE", "count": 1, "created": "2019-06-18T19:05:36+0500", "duration": 1296, "finished": "2019-06-18T19:05:38+0500", "values": { "from-cache": true }, "counters": { "count-read-cache": 1, "duration-prepare-result": 201, "duration-read-cache": 1008, "count-prepare-result": 1, "db-ops": 21 } }
```

У метода logging(), как и у subOperation(), есть параметр setup. В нём 
можно выставить как уровни логгирования, так и различные параметры LoggingObject.

```kotlin
object ValidateQuery : IKonveyorHandler<Context> {
    private val logger = LoggerFactory.getLogger(ValidateQuery::class.java)
    private val opName = "$KONVEYOR_OP_NAME/validate_query"
    
    fun exec(context: Context, env: IKonveyorEnvironment) = logging(logger, opName, setup = {
        startLevel = Level.INFO // ставим уровни, какие надо
        finishLevel = Level.DEBUG
        bodyLevel = Level.TRACE
        
        logobj.parent = env.get<LoggingObject>("parentLogger")
        //можно установить родителя, чтобы пробрасывать счётчики и параметры
        logobj.set("query", context.query) 
        // можно добавить параметры, чтобы они дописывались во все сообщения текущего логгера
    }) {
        when (context.queryVersion) {
            "current" -> return 
            "old" -> {
                logobj.set("deprecated-query", true, propagate=true)
                // propagate устанавливает значение и в родительском логгере
                return
            }
            else -> throw InvalidQueryException()
        }
    }
}
```

На выходе:
```
2019-06-25T16:56:11.449+0500 1519 INFO  DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#4] no-service c.s.s.c.provider.handler.Test - { "sessionId": "do_cool_stuff", "phase": "INIT", "state": "CREATED", "count": 1, "error_count": 0, "created": "2019-06-25T11:56:11.334+0000", "duration": 0 }
2019-06-25T16:56:11.464+0500 1534 INFO  DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#5] no-service c.s.s.c.p.handler.ValidateQuery - { "sessionId": "do_cool_stuff/validate_query", "phase": "INIT", "state": "CREATED", "count": 1, "error_count": 0, "created": "2019-06-25T11:56:11.456+0000", "duration": 0, "values": { "query": { "clazz": "codes.spectrum.sources.<source_name>.Context", "value": { "query": "old-query", "queryVersion": "old" } } } }
2019-06-25T16:56:11.467+0500 1537 DEBUG DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#5] no-service c.s.s.c.p.handler.ValidateQuery - { "sessionId": "do_cool_stuff/validate_query", "phase": "COMPLETE", "state": "COMPLETE", "count": 1, "error_count": 0, "created": "2019-06-25T11:56:11.456+0000", "duration": 11, "finished": "2019-06-25T11:56:11.467+0000", "values": { "query": { "clazz": "codes.spectrum.<source_name>.Context", "value": { "query": "old-query", "queryVersion": "old" } }, "deprecated-query": true } }
2019-06-25T16:56:11.468+0500 1538 INFO  DESKTOP-8UQ1DV5 [pool-2-thread-1 @coroutine#4] no-service c.s.s.c.provider.handler.Test - { "sessionId": "do_cool_stuff", "phase": "COMPLETE", "state": "COMPLETE", "count": 1, "error_count": 0, "created": "2019-06-25T11:56:11.334+0000", "duration": 134, "finished": "2019-06-25T11:56:11.468+0000", "values": { "deprecated-query": true } }
```

## Конфигурация logback в проектах

Проработана обновленная структура файла Logback в проектах
```xml
<configuration
        xmlns="http://ch.qos.logback/xml/ns/logback"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://ch.qos.logback/xml/ns/logback https://raw.githubusercontent.com/enricopulatzo/logback-XSD/master/src/main/xsd/logback.xsd"
>
    <property scope="system" name="service" value="${SERVICE_NAME:-no-service}"/>
    <property scope="system" name="host" value="${HOSTNAME:-no-host}"/>
    <property scope="system" name="amqp_appender_on" value="${LOG_AMQP_ON:-false}"/>
    <property scope="system" name="amqp_appender_vhost" value="${LOG_AMQP_VHOST:-NONE}"/>

    <!-- позволяет перекрыть файл на уровне сервиса внутри докера и добавить в design-time или runtime нужную конфигурацию -->
    <include file="${LOG_EXTENSION_FILE:-./logback-extension.xml}" optional="true"/>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>${LOG_CONSOLE_LEVEL:-${LOG_ROOT_LEVEL:-WARN}}</level>
        </filter>
        <encoder>
            <!-- улучшенный паттерн - возможность установить из ENV, вывод времени с начала работы, полная дата- время, гарантия однострочных сообщений, ограничение длины stacktrace -->
            <pattern>${LOG_DEFAULT_LAYOUT:-%-16(%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} %r %-5level %property{host} [%thread] %property{service} %logger{36}) - %replace(%msg){'\s+',' '}%n%ex{10}}</pattern>
        </encoder>
    </appender>


    <if condition='property("amqp_appender_on").contains("true") || property("amqp_appender_on").contains("all")'>
        <then>
            <appender name="amqp"
                      class="org.springframework.amqp.rabbit.logback.AmqpAppender">
                <filter class="codes.spectrum.logging.logback.SetupUnifiedMDCFilter" />
                <encoder class="codes.spectrum.logging.logback.UnifiedJsonEncoder"/>
                <host>${LOG_AMQP_HOST:-127.0.0.1}</host>
                <port>${LOG_AMQP_PORT:-5672}</port>
                <if condition='property("amqp_appender_vhost").contains("NONE")'>
                    <then>
                    </then>
                    <else>
                        <virtualHost>${LOG_AMQP_VHOST}</virtualHost>
                    </else>
                </if>
                <username>${LOG_AMQP_USER:-guest}</username>
                <password>${LOG_AMQP_PASSWORD:-guest}</password>
                <exchangeType>topic</exchangeType>
                <exchangeName>${LOG_AMQP_EXCHANGE:-log}</exchangeName>
                <applicationId>${SERVICE_NAME:-no-service}</applicationId>
                <routingKeyPattern>${LOG_AMQP_ROUTE_PATTERN:-${LOG_AMQP_PREFIX:-log}.%level.%property{service}.%X{operation:-none}.%X{phase:-none}.%X{state:-none}.%X{user:-none}.%X{session:-none}.%X{logger-tail:-none}.%property{host}}</routingKeyPattern>
                <contentType>text/plain</contentType>
                <maxSenderRetries>${LOG_AMQP_RETRY:-3}</maxSenderRetries>
                <charset>UTF-8</charset>
            </appender>
        </then>
    </if>

    <root level="${LOG_ROOT_LEVEL:-WARN}">
        <appender-ref ref="STDOUT"/>
        <if condition='property("amqp_appender_on").contains("all")'>
            <then>
                <appender-ref ref="amqp"/>
            </then>
        </if>
    </root>

    <logger name="codes.spectrum" level="${LOG_SPECTRUM_LEVEL:-DEBUG}">
        <if condition='property("amqp_appender_on").contains("true") || property("amqp_appender_on").contains("spectrum")'>
            <then>
                <appender-ref ref="amqp"/>
            </then>
        </if>
    </logger>

</configuration>
```
### Общие настройки

|#|Имя|Тип|По умолчанию|Комментарий|
|----|----|----|----|----|
|1|`LOG_ROOT_LEVEL`|`enum(TRACE,DEBUG,INFO,WARN,ERROR)`|`WARN`|Уровень записи для всех журналов, включая внешние классы|
|2|`LOG_SPECTRUM_LEVEL`|`enum(TRACE,DEBUG,INFO,WARN,ERROR)`|`DEBUG`|Уровень записи для журналов наших классов|
|3|`LOG_CONSOLE_LEVEL`|`enum(TRACE,DEBUG,INFO,WARN,ERROR)`|`${LOG_SPECTRUM_LEVEL}`|дополнительный опциональный уровень именно для консоли (чтобы можно было ограничить)|
|4|`LOG_EXTENSION_FILE`|`path`|`./logback-extension.xml`|Файл, который можно установить в докер в качестве кастомной настройки|
|5|`SERVICE_NAME`|`string`|`providerd_by_service|no-service`|Имя приложения, службы (не является настройкой только для логов, системная)|
|6|`LOG_DEFAULT_LAYOUT`|`string`|см. конфиг|Паттерн для отправки сообщения в STD OUT|

### Настройки для использования с RabbitMQ
|#|Имя|Тип|По умолчанию|Комментарий|
|----|----|----|----|----|
|1|`LOG_AMQP_ON`|`enum(false,spectrum(==true(fallback)),all)`|`false`|Включить запись в RABBITMQ - `true` или `spectrum` - учитываются только события "наших" классов, `all` - всех, включая системные|
|1|`LOG_AMQP_LEVEL`|`enum(TRACE,DEBUG,INFO,WARN,ERROR)`|`INFO`|Уровень журнала для AMQP|
|2|`LOG_AMQP_HOST`|`hostname/ip`|`127.0.0.1`|Адрес сервера RABBITMQ|
|3|`LOG_AMQP_PORT`|`int`|`5672`|Порт сервера RABBITMQ|
|4|`LOG_AMQP_VHOST`|`string`|``|Виртуальный хост RABBITMQ|
|5|`LOG_AMQP_USER`|`string`|`guest`|Пользоваетель RABBITMQ|
|6|`LOG_AMQP_PASSWORD`|`string`|`guest`|Пароль пользователя RABBITMQ|
|7|`LOG_AMQP_EXCHANGE`|`string`|`log`|Имя обменника RABBITMQ (должен быть создан)|
|8|`LOG_AMQP_ROUTE_PREFIX`|`string`|`log`|префикс роутинга|
|8|`LOG_AMQP_ROUTE_PATTERN`|`string`|см. конфиг|Паттерн разрисовки routing-key (включает `LOG_AMQP_ROUTE_PREFIX` )|
|9|`LOG_AMQP_RETRY`|`int`|`3`|Максимальное число попыток отправки|

> Внимание! Дефлотный баланс следующий - системные классы логируются только в консоль и  на уровне WARN,
а свои классы логируются в консоль на уровне DEBUG (на проде рекломендуется вытсавить INFO)
и при этом опционально при включении `LOG_AMQP_ON` пишутся в RabbitMQ на уровне INFO

### Формат вывода в консоль по умолчанию
Установлен следующий формат:
```
%-16(%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} %r %-5level %property{host} [%thread] %property{service} %logger{36}) - %replace(%msg){'\s+',' '}%n%ex{10}
```
По позициям (разделители - пробелы или несколько пробелов при выравнивании)
1. Таймстамп сообщения в формате `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, например `2019-06-16T17:41:55.138+0500`
2. Относительное время в ms от старта приложения, например `57333`
3. Уровень сообщения, например `INFO`
4. Имя хоста, берется из `HOSTNAME`
5. Имя потока в квадратных строках (формат недетерминирован), например `[thread-1 @coroutine-2]`
6. Имя сервиса - берется из `SERVICE_NAME`
7. Имя журнала в сжатом варианте, например `i.k.r.junit5.JUnitTestRunnerListener`
8. `-` - разделяет заголовки сообщения от сообщения
9. До конца строки идет сообщение, гарантировано однострочное
10. Перенос
11. Опционально (при ошибках)
    - Класс ошибки
    - ':' разделитель
    - Сообщение ошибки до конца строки
    - Перенос
    - До десяти строк стектрейса ошибок


Пример сообщения без трейса:
```
2019-06-16T17:41:55.136+0500 570 DEBUG [DESKTOP-GHGCB6U Test worker] sample-service io.kotlintest.runner.jvm.TestEngine  - Submitting 1 specs
```

Пример сообщения с трейсом
```
2019-06-16T18:02:24.695+0500 100 INFO  DESKTOP-GHGCB6U [thread-1] sample-service some.logger - multi string with spaces
java.lang.Exception: test
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt:27)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt:26)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt:25)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt:24)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt:23)
	at codes.spectrum.logging.logback.DefaultStdOutLayoutTestKt$error$1$1$1$1$1$1.invoke(DefaultStdOutLayoutTest.kt)
```

Регексп для разбора
Если только заголовок разбирать:
`(\S+)\s*(\S+)\s*(\S+)\s*(\S+)\s*\[([^\]]+)\]\s*(\S+)\s*(\S+)\s*-\s*([^\r\n]+)`
По номерам групп
1. `$1` - таймстамп
2. `$2` - время от запуска
3. `$3` - уровень журнала
4. `$4` - хост
5. `$5` - поток
6. `$6` - сервис
7. `$7` - журнал
8. `$8` - сообщение

Если и с телом ошибок, то `(\S+)\s*(\S+)\s*(\S+)\s*(\S+)\s*\[([^\]]+)\]\s*(\S+)\s*(\S+)\s*-\s*([^\r\n]+)(\s*(\S+):\s*(\S+)([^\r\n]*[\r\n]+((\s*at\s*[^\r\n]+[\r\n]*))*)?)?` 
Тогда добавляется
1. `$10` - тип ошибки
2. `$11` - сообщение ошибки
3. `$12` - стектрейс

## Сообщения в RABBITMQ


Роут формаируется в следующем формате:
`${LOG_AMQP_PREFIX:-log}.%level.%property{service}.%X{operation:-none}.%X{phase:-none}.%X{state:-none}.%X{user:-none}.%X{session:-none}.%X{logger-tail:-none}.%property{host}`
> Внимание! Проброс параметров гарантируется наличием в конфиге `<filter class="codes.spectrum.logging.logback.SetupUnifiedMDCFilter" />`

Пример роута

`log.DEBUG.billing.generate-report--prepare-query.INIT.CREATED.comdiv.1560872365189.std.DESKTOP-GHGCB6U`

точками разделены следующие позиции

1. `log` - константа
2. `level` - уровень журнала
3. `service` - имя сервиса (или `none`)
4. `operation` - имя операции (или `none`) (слеши заменены двойным дефисом)
5. `phase` - фаза обработки или `none`
6. `state` - статус обработки или `none`
7. `user` - имя пользователя или `none`
8. `session` - номер сессии или `none`
9. `logger-tail` - последнее слово (обычно класс) в имени логгера
10. `host` - имя хоста


Соответственно при настройках биндинга для перехвата сообщений - собственно должно использоваться
администратором для диспетчеризации журналов

1. Перехват всех логов - `#`
2. Перехват всех ERROR - `log.ERROR.#`
3. Перехват всех сообщений нужного сервиса `log.*.my-service.#`
4. Перехват конкретной сессии `log.#.123443.#`
5. Перехват всех сообщений определенного пользователя `log.#.comdiv.#`
6. Перехват всех сообщений отдельного хоста `#.QIOIO23-940390`

Тело сообщения в RABBITMQ `всегда` форматируются в JSON и содержат в себе все параметры лога.
Это осуществляется автоматически при помощи `codes.spectrum.logging.logback.UnifiedJsonEncoder`

Не важно - JSON или просто строка была на входе - она конвертируется в JSON и обогащается параметрами журнала

1. timestamp - момент записи лога, ms
2. logger - имя логгера 
3. level - уровень журнала
4. error - ошибка (если есть)
5. thread - имя потока
6. hostName - хост логгера
7. serviceName - имя сервиса
8. timeFromStart - время от начала работы сервиса, ms

Пример сообщения в RABBITMQ:

```json
{
  "message": "hello world",
  "hostName": "THE-HOST",
  "timestamp": 1560864321611,
  "serviceName": "the-service",
  "level": "TRACE",
  "logger": "codes.spectrum.logging.logback.RabbitAppenderTest",
  "timeFromStart": 1799,
  "thread": "pool-3-thread-1 @coroutine#6",
  "error": {
    "type": "java.lang.Exception",
    "message": "x",
    "cause": {
      "type": "java.lang.Exception",
      "message": "y",
      "stack": [
        "codes.spectrum.logging.logback.RabbitAppenderTest$1$2.invokeSuspend at RabbitAppenderTest.kt:88",
        "codes.spectrum.logging.logback.RabbitAppenderTest$1$2.invoke at RabbitAppenderTest.kt:-1",
        "io.kotlintest.runner.jvm.TestCaseExecutor$executeTest$supervisorJob$1$invokeSuspend$$inlined$map$lambda$1.invokeSuspend at TestCaseExecutor.kt:121"
      ]
    },
    "stack": [
      "codes.spectrum.logging.logback.RabbitAppenderTest$1$2.invokeSuspend at RabbitAppenderTest.kt:88",
      "codes.spectrum.logging.logback.RabbitAppenderTest$1$2.invoke at RabbitAppenderTest.kt:-1",
      "io.kotlintest.runner.jvm.TestCaseExecutor$executeTest$supervisorJob$1$invokeSuspend$$inlined$map$lambda$1.invokeSuspend at TestCaseExecutor.kt:121"
    ]
  }
}
```




## Перегрузка конфигурации

У Logback есть все возможности для перезагрузки и чтения конфигурации заново или добавления новых конфигураций,
для этого добавлены расширения:
```kotlin
 codes.spectrum.logging.logback.logbackReconfigure(source) // загружает указанную конфигурацию из строки, файла или потока
 codes.spectrum.logging.logback.logbackReconfigureToDefault() // восстанавливает конфигурацию по умолчанию
```

> Планируется во все контейнеры на уровне REST добавить endpoint `POST /api/v2/{service}/admin/log/setup{body:XML}` 
и `GET /api/v2/{service}/admin/log/reset` для возможности полного управления картой логирования в runtime


# Запуск локального демо-стенда

Чтобы испытать все локально можно использовать `docker-compose` в папке `./elk`

> Важно проследить, чтобы порты 5672,15672,5601,9200,9300 еще не были заняты локальными службами или нужно
перенастроить их в docker-compose

```bash
cd ./elk
docker-compose up -d
```
После старта у Вас становятся  доступными:
1. `127.0.0.1:5672` - RabbitMQ
2. `127.0.0.1:15672` - Админка RabbitMQ
3. `127.0.0.1:9200` - ElasticSearch
4. `127.0.0.1:5601` - Kibana
5. В рэббите настроен exchange log, очередь demolog и биндинг log(#)->demolog (можно поменять в `./elk/rabbit.json`)
6. Запущен логстеш, который мониторит очередь demolog и пишет в инедкс эластика `demolog-{YYYY-MM-dd}` (можно поменять в `./elk/logstash.conf`)

После этого можно запускать приложение с `LOG_AMQP_ON : true|spectrum|all` и с настройками по умолчанию
все должно работать - все должено быть сохранено в индекс `demolog-{YYYY-MM-dd}` при необходимости можно 
настроить визуализацию Kibana


# Примерный интеграционные сценарий

Сценарии ограничения уровня не особо интересны в силу очевидности - ниже приведены сценарии
именно большого объема на выходе с малым объемом обработки - что нужно на стейджах, при отладке и ловле багов

## Самый максимальный уровень отправки лога из приложения при минимальном уровне чтения и хранения
Этот режим увеличивает нагрузку на RABBITMQ, но при этом позволяет вести максимально детальный лог.

1. `LOG_ROOT_LEVEL` - `TRACE`  - ловим все сообщения от всех системных классов
2. `LOG_SPECTRUM_LEVEL` - `TRACE` - в лог пойдут все наши сообщения
3. `LOG_CONSOLE_LEVEL` - `WARN` или `ERROR` - при этом консоль не засоряется лишними сообшениями
4. `LOG_AMQP_ON` - `all` - все сообщения пойдут в RabbitMQ

## Максимальный уровень для наших классов и только ошибки в консоль от системных

1. `LOG_ROOT_LEVEL` - `ERROR`  - ловим все сообщения от всех системных классов
2. `LOG_SPECTRUM_LEVEL` - `TRACE` - в лог пойдут все наши сообщения
3. `LOG_AMQP_ON` - `spectrum` - все сообщения пойдут в RabbitMQ


## Решение проблемы нагрузки на RabbitMQ и приложение

1. Уровень `DEBUG` и `TRACE` дают повышенную нагрузку как на приложение так и на RabbitMQ
2. Если речь идет о записи в незабинженный Exchange - RabbitMQ нагружается не сильно, но сетевой траффик формируется
3. Больше проблемы если очередь с трейсами не разгребать или там скопиться очень много сообещний

Чтобы решить эти проблемы

1. Не биндить без нужды сообщения ниже INFO на очереди
2. Не включать без нужды уровень сообщений на приложении выше INFO на проде и выше DEBUG на стейдже
3. В случае острой необходимости поймать трейс-лог использовать рантайм - переконфигурацию logback с
установкой другой конфигурации

## Другие аппендеры и логгеры

Если возникнет нужда в подключении дополнительных аппендеров или роутов

1. Если вопрос стоит "узаконить" некий общесистемный вариант (какой-то SMTP, UDP отправщик ) и т.п.
и сделать его поддержку во все сервисы - надо сформуллировать задачу
2. Если требуется временная запись в какое-то альтернативное место - использовать переконфигурацию
