package kokoro.app.ui.engine.window.jcef

import org.cef.browser.CefBrowser
import org.cef.handler.CefFocusHandlerAdapter
import java.awt.EventQueue
import java.awt.KeyboardFocusManager
import java.util.concurrent.atomic.AtomicBoolean

internal class CefFocusHandlerImpl : CefFocusHandlerAdapter(), Runnable {

	override fun onGotFocus(browser: CefBrowser) {
		val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
		if (focusOwner != null) {
			val c = browser.uiComponent
			/** Ensure a permanent focus owner once we clear the focus.
			 * @see KeyboardFocusManager.getPermanentFocusOwner */
			if (focusOwner !== c) EventQueue.invokeLater { c.requestFocusInWindow() }
			// The following would clear any focus owner.
			if (clearingGlobalFocus.compareAndSet(false, true)) EventQueue.invokeLater(this)
		}
	}

	override fun run() {
		// Necessary for the JCEF browser to play nicely with other AWT
		// components; otherwise, focus and traversal of AWT components won't
		// work properly. That is, there must be no focused AWT component while
		// a JCEF browser has focus.
		KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
		EventQueue.invokeLater(clearingGlobalFocus)
	}

	// NOTE: The boolean value represents "dispatched" when `true`, and
	// "undispatched" when `false`.
	private val clearingGlobalFocus = object : AtomicBoolean(), Runnable {
		override fun run() = set(false) // Allow redispatch
	}
}
