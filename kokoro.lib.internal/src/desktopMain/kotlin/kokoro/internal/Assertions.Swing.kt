package kokoro.internal

import javax.swing.SwingUtilities

@Suppress("NOTHING_TO_INLINE")
inline fun assertThreadSwing() = assert({
	"Swing EDT expected! Actual thread: ${Thread.currentThread().name}"
}) { SwingUtilities.isEventDispatchThread() }
