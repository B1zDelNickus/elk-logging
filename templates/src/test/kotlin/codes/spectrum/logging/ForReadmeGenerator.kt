package codes.spectrum.logging

import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class ForReadmeGenerator : StringSpec({
    "!:print default out for no-hand log"{
        SomeService().execute()
    }
    "!:print errors"{
        SomeService().execute(true)
    }

    "!:custom log"{
        SomeService().execute(false)
    }
}){
    class SomeService(
        // у сервиса должен быть логер и его лучше иметь возможность передать
        // как параметр конструктоа
        val logger: Logger = LoggerFactory.getLogger(SomeService::class.java),
        // также у сервиса должно быть имя операции, которую он выполняет и лучше бы
        // оно настраивалось
        val operationName : String = "do-cool-work"
    ){

        // некая значимая функция
        fun execute(error:Boolean = false):String =// собственно вход в функцию
            logging(logger,operationName){
                val rootLog = this
                var cacheValue = ""
                subOperation("read-cache",setup={
                    setAllLevels(Level.INFO)
                }){
                    delay(1000)
                    cacheValue = "123" // имитация некоей первой операции с БД
                    logobj.values["cachedValue"] = cacheValue
                    rootLog.logobj.values["from-cache"] = true
                    rootLog.logobj.inc("db-ops")
                    log()
                }
                var result = ""
                subOperation("prepare-result"){
                    delay(200)
                    if(error){
                        throw Exception("what the hell!!!")
                    }
                    rootLog.logobj.add("db-ops",20)
                    result = cacheValue+"-result"
                }
                result
            }
    }
}

