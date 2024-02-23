package kokoro.internal

@Suppress(NOTHING_TO_INLINE)
actual inline fun isThreadMain(): Boolean = true

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal actual fun checkThreadMain_lazyMessage(): String = error("Unreachable")
