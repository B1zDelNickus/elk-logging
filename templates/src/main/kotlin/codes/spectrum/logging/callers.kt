package codes.spectrum.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T,R> logging(operation: String? = null, noinline setup: LoggingContext.() -> Unit = {}, noinline body: suspend LoggingContext.() ->R):R =
    logging<R>(LoggerFactory.getLogger(T::class.java), operation, setup, body)

inline fun <reified T,R> logging(noinline setup: LoggingContext.() -> Unit = {}, noinline  body: suspend LoggingContext.() -> R):R =
    logging<R>(LoggerFactory.getLogger(T::class.java), setup, body)


inline fun <R>  LoggingContext?.rootOrSuboperation(logger: Logger,
                                                             operation: String = "",
                                                             noinline setup: LoggingContext.() -> Unit = {},
                                                             noinline body: suspend LoggingContext.() -> R):R {
    if(null==this)return logging(logger,operation,setup,body)
    return this.subOperation(operation,setup,body)
}

fun <R> logging(
    logger: Logger,
    setup: LoggingContext.() -> Unit = {},
    body: suspend LoggingContext.() -> R):R {
    return logging(logger, null, setup, body)
}

fun <R> logging(
    logger: Logger,
    operation: String? = null,
    setup: LoggingContext.() -> Unit = {},
    body: suspend LoggingContext.() -> R):R {
    val context = LoggingContext(logger, LoggingObject(operation=operation)).also(setup)
    return logging(context, body)
}

fun <R> logging(
    context: LoggingContext,
    body: suspend LoggingContext.() -> R):R {
    return context.execute(body)
}


