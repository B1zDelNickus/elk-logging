package codes.spectrum.logging.slf4j.internals

import codes.spectrum.logging.LoggerCall
import org.slf4j.Marker
import org.slf4j.MarkerFactory

internal val LoggerCall.slfMarker: Marker get() = if (null == marker) throw Exception("No marker defined") else MarkerFactory.getMarker(marker!!)

internal fun unifyArg(arg: Any?): List<Any?> {
    if (null == arg) return emptyList()
    if (arg is List<*>) return arg
    if (arg is Array<*>) return arg.toList()
    return listOf(arg)
}