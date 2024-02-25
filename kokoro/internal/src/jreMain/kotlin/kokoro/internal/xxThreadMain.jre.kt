package kokoro.internal

import java.awt.EventQueue

@Suppress("NOTHING_TO_INLINE")
actual inline fun isThreadMain(): Boolean = EventQueue.isDispatchThread()

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal actual fun checkThreadMain_lazyMessage(): String =
	"Swing EDT expected! Actual thread: ${Thread.currentThread().name}"
