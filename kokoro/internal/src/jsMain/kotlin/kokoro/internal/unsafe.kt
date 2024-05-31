package kokoro.internal

@Suppress("NOTHING_TO_INLINE")
inline fun <T> unsafeNull(): T = @Suppress("UnsafeCastFromDynamic") null.asDynamic()
