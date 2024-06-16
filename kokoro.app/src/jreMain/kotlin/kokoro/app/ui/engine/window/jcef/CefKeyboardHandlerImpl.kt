package kokoro.app.ui.engine.window.jcef

import kokoro.app.ui.engine.window.WvWindowFrame
import kokoro.app.ui.swing.jcef.toKeyEvent
import org.cef.browser.CefBrowser
import org.cef.handler.CefKeyboardHandler
import org.cef.handler.CefKeyboardHandlerAdapter
import java.awt.EventQueue
import java.awt.KeyboardFocusManager

internal class CefKeyboardHandlerImpl(private val owner: WvWindowFrame) : CefKeyboardHandlerAdapter() {
	override fun onKeyEvent(browser: CefBrowser?, e: CefKeyboardHandler.CefKeyEvent?): Boolean {
		if (e != null) run<Unit> {
			val owner = owner
			val jcef = owner.jcef ?: return@run
			if (jcef.browser !== browser) return@run
			// Allow Swing/AWT to see and intercept CEF key events.
			// - See also, https://www.magpcss.org/ceforum/viewtopic.php?f=17&t=17305
			val ke = e.toKeyEvent(owner)
			EventQueue.invokeLater {
				// NOTE: By the time we get here, `owner.jcef` may now be null,
				// which is as it should be after being torn down.
				owner.jcef?.component?.parent?.let { c ->
					val kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager()
					// NOTE: The following treats `c` as if it is the focus
					// owner (even if it isn't).
					kfm.redispatchEvent(c, ke)
				}
			}
			return true
		}
		return false
	}
}
