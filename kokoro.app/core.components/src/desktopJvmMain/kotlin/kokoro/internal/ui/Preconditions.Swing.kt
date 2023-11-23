package kokoro.internal.ui

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.assert
import java.awt.EventQueue
import javax.swing.SwingUtilities

@Suppress("NOTHING_TO_INLINE")
inline fun assertThreadSwing() = assert({
	@Suppress("DEPRECATION") `-checkThreadSwing-lazyMessage`()
}) { SwingUtilities.isEventDispatchThread() }

@Suppress("NOTHING_TO_INLINE")
inline fun checkThreadSwing() = check(lazyMessage = {
	@Suppress("DEPRECATION") `-checkThreadSwing-lazyMessage`()
}, value = EventQueue.isDispatchThread())

@Deprecated(SPECIAL_USE_DEPRECATION)
@PublishedApi
internal fun `-checkThreadSwing-lazyMessage`(): String =
	"Swing EDT expected! Actual thread: ${Thread.currentThread().name}"
