package codes.spectrum.logging

import org.slf4j.event.Level

/**
 * Используется при авто-диспатчинге уровней при вызове `Logger.log(message:Any)`
 * особенно удобно для логирования исключений
 */
interface ILogLevelProvider {
    fun getLogLevel(): Level = Level.INFO
}