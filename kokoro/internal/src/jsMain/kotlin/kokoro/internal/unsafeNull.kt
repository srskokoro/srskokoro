package kokoro.internal

@Suppress("NOTHING_TO_INLINE")
inline fun <T> unsafeNull(): T = null.asDynamic().unsafeCast<T>()
