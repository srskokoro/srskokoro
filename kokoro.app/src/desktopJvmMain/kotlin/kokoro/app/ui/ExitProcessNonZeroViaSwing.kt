package kokoro.app.ui

import kokoro.app.ui.ExitProcessNonZeroViaSwing.Companion.install
import kokoro.internal.system.cleanProcessExit
import kokoro.internal.ui.assertThreadSwing
import java.awt.AWTEvent
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.AWTEventListener
import java.awt.event.WindowEvent
import java.util.IdentityHashMap

/**
 * A mechanism that can be installed to ensure that the process will exit with a
 * non-zero status code after all opened windows are closed and all events have
 * been processed by the Swing EDT.
 *
 * @see install
 */
class ExitProcessNonZeroViaSwing private constructor(private val toolkit: Toolkit) : AWTEventListener, Runnable {

	private val openedWindows = IdentityHashMap<Window, Unit>().also {
		for (w in Window.getWindows())
			if (w.isShowing) it[w] = Unit
	}

	companion object {

		/** WARNING: Must be called on the Swing EDT. */
		fun install() {
			assertThreadSwing()
			val toolkit = Toolkit.getDefaultToolkit()
			val listener = ExitProcessNonZeroViaSwing(toolkit)
			toolkit.addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK)
			if (listener.openedWindows.isEmpty()) listener.tryExitSoon()
		}
	}

	override fun eventDispatched(e: AWTEvent) {
		if (e.id == WindowEvent.WINDOW_OPENED) {
			val w = e.source
			if (w is Window) {
				openedWindows[w] = Unit
			}
		} else if (e.id == WindowEvent.WINDOW_CLOSED) {
			val ws = openedWindows
			if (ws.remove(e.source) != null && ws.isEmpty()) {
				tryExitSoon()
			}
		}
	}

	private var exiting = false

	@Suppress("NOTHING_TO_INLINE")
	private inline fun tryExitSoon() {
		if (!exiting) {
			exiting = true
			EventQueue.invokeLater(this)
		}
	}

	override fun run() {
		if (openedWindows.isNotEmpty()) {
			// Can't exit yet
			exiting = false
		} else {
			if (toolkit.systemEventQueue.peekEvent() != null) {
				// Reschedule until we're at the end of the event queue
				EventQueue.invokeLater(this)
			} else {
				// Done! Exit now!
				cleanProcessExit(StackTraceModal.NONZERO_STATUS)
			}
		}
	}
}
