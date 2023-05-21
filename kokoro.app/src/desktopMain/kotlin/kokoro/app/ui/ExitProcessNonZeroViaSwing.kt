package kokoro.app.ui

import kokoro.app.ui.ExitProcessNonZeroViaSwing.Companion.install
import kokoro.internal.ui.assertThreadSwing
import java.awt.AWTEvent
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.AWTEventListener
import java.awt.event.WindowEvent
import java.util.IdentityHashMap
import kotlin.system.exitProcess

/**
 * A mechanism that can be installed to ensure that the process will exit with a
 * non-zero status code after all opened windows are closed.
 *
 * @see install
 */
class ExitProcessNonZeroViaSwing private constructor(private val toolkit: Toolkit) : AWTEventListener, Runnable {
	companion object {

		fun install() {
			assertThreadSwing()
			val toolkit = Toolkit.getDefaultToolkit()
			val listener = ExitProcessNonZeroViaSwing(toolkit)
			if (listener.openedWindows.isEmpty()) exitNow()
			toolkit.addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK)
		}

		@Suppress("NOTHING_TO_INLINE") // Why not? It's `private` code anyway :P
		private inline fun exitNow(): Nothing {
			exitProcess(StackTraceModal.NONZERO_STATUS)
		}
	}

	private val openedWindows = IdentityHashMap<Window, Unit>().also {
		for (w in Window.getWindows())
			if (w.isShowing) it[w] = Unit
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
				exitNow()
			}
		}
	}
}
