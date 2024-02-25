package kokoro.internal

@Suppress("NOTHING_TO_INLINE")
actual inline fun isThreadMain(): Boolean = Thread.currentThread() == mainThread

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal actual fun checkThreadMain_lazyMessage(): String =
	"Main thread expected! Actual thread: ${Thread.currentThread().name}"
