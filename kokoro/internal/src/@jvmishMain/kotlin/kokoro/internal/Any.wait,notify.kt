@file:Suppress("NOTHING_TO_INLINE", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package kokoro.internal

/** @see java.lang.Object.wait */
@Throws(InterruptedException::class)
inline fun Any.wait() = (this as Object).wait()

/** @see java.lang.Object.wait */
@Throws(InterruptedException::class)
inline fun Any.wait(timeoutMillis: Long) = (this as Object).wait(timeoutMillis)

/** @see java.lang.Object.wait */
@Throws(InterruptedException::class)
inline fun Any.wait(timeoutMillis: Long, nanos: Int) = (this as Object).wait(timeoutMillis, nanos)

/** @see java.lang.Object.notifyAll */
inline fun Any.notifyAll() = (this as Object).notifyAll()
