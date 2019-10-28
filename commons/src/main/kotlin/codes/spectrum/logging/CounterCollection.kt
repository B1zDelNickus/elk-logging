package codes.spectrum.logging

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class CounterCollection : ConcurrentHashMap<String, AtomicLong>() {
    fun check(name: String) = add(name, 0)
    fun inc(name: String) = add(name, 1)
    fun add(name: String, increment: Long): Long {
        if (increment == 0L) {
            return get(name)?.get() ?: 0L
        } else {
            return getOrPut(name) { AtomicLong() }.addAndGet(increment)
        }
    }
}