package service

import java.util.concurrent.atomic.AtomicLong

object AccountNumberGenerator {

    val current: AtomicLong = new AtomicLong()

    def next(): String = {
        val suffix = current.incrementAndGet()
        f"${suffix}%010d"
    }

    /**
      * Only for tests
      */
    def clear()=
        current.set(0)
}
