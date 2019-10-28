package codes.spectrum.logging.slf4j

import codes.spectrum.logging.LoggerCall
import codes.spectrum.logging.slf4j.internals.slfMarker
import org.slf4j.Logger
import org.slf4j.Marker as Marker1

/**
 * Специализированный trace для LoggerCall - собственно
 * адаптирует вызовы SLF
 */
fun Logger.trace(message: LoggerCall): Boolean {
    if (!this.isTraceEnabled) return false
    when (message.error) {
        null -> {
            when (message.marker) {
                null -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            trace(message.message)
                        }
                        else -> {
                            trace(message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
                else -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            trace(message.slfMarker, message.message)
                        }
                        else -> {
                            trace(message.slfMarker, message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
            }
        }
        else -> {
            when (message.marker) {
                null -> {
                    trace(message.message, message.error!!)
                }
                else -> {
                    trace(message.slfMarker, message.message, message.error!!)
                }
            }
        }
    }
    return true
}

/**
 * Специализированный debug для LoggerCall - собственно
 * адаптирует вызовы SLF
 */
fun Logger.debug(message: LoggerCall): Boolean {
    if (!this.isDebugEnabled) return false
    when (message.error) {
        null -> {
            when (message.marker) {
                null -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            debug(message.message)
                        }
                        else -> {
                            debug(message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
                else -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            debug(message.slfMarker, message.message)
                        }
                        else -> {
                            debug(message.slfMarker, message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
            }
        }
        else -> {
            when (message.marker) {
                null -> {
                    debug(message.message, message.error!!)
                }
                else -> {
                    debug(message.slfMarker, message.message, message.error!!)
                }
            }
        }
    }
    return true
}

/**
 * Специализированный info для LoggerCall - собственно
 * адаптирует вызовы SLF
 */
fun Logger.info(message: LoggerCall): Boolean {
    if (!this.isInfoEnabled) return false
    when (message.error) {
        null -> {
            when (message.marker) {
                null -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            info(message.message)
                        }
                        else -> {
                            info(message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
                else -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            info(message.slfMarker, message.message)
                        }
                        else -> {
                            info(message.slfMarker, message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
            }
        }
        else -> {
            when (message.marker) {
                null -> {
                    info(message.message, message.error!!)
                }
                else -> {
                    info(message.slfMarker, message.message, message.error!!)
                }
            }
        }
    }
    return true
}

/**
 * Специализированный warn для LoggerCall - собственно
 * адаптирует вызовы SLF
 */
fun Logger.warn(message: LoggerCall): Boolean {
    if (!this.isWarnEnabled) return false
    when (message.error) {
        null -> {
            when (message.marker) {
                null -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            warn(message.message)
                        }
                        else -> {
                            warn(message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
                else -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            warn(message.slfMarker, message.message)
                        }
                        else -> {
                            warn(message.slfMarker, message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
            }
        }
        else -> {
            when (message.marker) {
                null -> {
                    warn(message.message, message.error!!)
                }
                else -> {
                    warn(message.slfMarker, message.message, message.error!!)
                }
            }
        }
    }
    return true
}

/**
 * Специализированный error для LoggerCall - собственно
 * адаптирует вызовы SLF
 */
fun Logger.error(message: LoggerCall): Boolean {
    if (!this.isErrorEnabled) return false
    when (message.error) {
        null -> {
            when (message.marker) {
                null -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            error(message.message)
                        }
                        else -> {
                            error(message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
                else -> {
                    when {
                        message.args.isNullOrEmpty() -> {
                            error(message.slfMarker, message.message)
                        }
                        else -> {
                            error(message.slfMarker, message.message, *message.args!!.toTypedArray())
                        }
                    }
                }
            }
        }
        else -> {
            when (message.marker) {
                null -> {
                    error(message.message, message.error!!)
                }
                else -> {
                    error(message.slfMarker, message.message, message.error!!)
                }
            }
        }
    }
    return true
}
