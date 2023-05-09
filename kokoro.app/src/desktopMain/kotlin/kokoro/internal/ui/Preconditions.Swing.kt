package kokoro.internal.ui

import kokoro.internal.assert
import java.awt.EventQueue
import javax.swing.SwingUtilities

@Suppress("NOTHING_TO_INLINE")
inline fun assertThreadSwing() = assert({
	"Swing EDT expected! Actual thread: ${Thread.currentThread().name}"
}) { SwingUtilities.isEventDispatchThread() }

@Suppress("NOTHING_TO_INLINE")
inline fun checkThreadSwing() = check(lazyMessage = {
	"Swing EDT expected! Actual thread: ${Thread.currentThread().name}"
}, value = EventQueue.isDispatchThread())
