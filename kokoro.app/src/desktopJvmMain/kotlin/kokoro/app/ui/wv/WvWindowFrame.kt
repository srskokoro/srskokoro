package kokoro.app.ui.wv

import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.dp
import kokoro.app.ui.ScopedWindowFrame
import kokoro.jcef.Jcef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.AWTEvent
import java.awt.Component
import java.awt.GraphicsConfiguration
import java.awt.event.ComponentEvent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class WvWindowFrame @JvmOverloads constructor(
	context: CoroutineContext = EmptyCoroutineContext,
	title: String = DEFAULT_TITLE,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : ScopedWindowFrame(context, title, gc) {

	class JcefState(
		val client: CefClient,
		val browser: CefBrowser,
		val component: Component,
	)

	private var _jcef: JcefState? = null
	val jcef get() = _jcef

	override fun addNotify(): Unit = synchronized(treeLock) {
		if (_jcef == null) {
			// TODO Hook some logging, so that we can detect errors, just like
			//  in the JCEF Maven example app.

			val client = Jcef.app.createClient()
			client.addLoadHandler(object : CefLoadHandlerAdapter() {
				override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
					// TODO Maybe use `DOMContentLoaded` instead?
					//  See, https://magpcss.org/ceforum/viewtopic.php?t=10277

					// TODO Set up
				}
			})

			// TODO Properly set up things
			val browser = client.createBrowser("https://example.com", false, false)

			val component = browser.uiComponent
			contentPane.add(component)

			_jcef = JcefState(client, browser, component)
		}
		super.addNotify()
	}

	override fun removeNotify(): Unit = synchronized(treeLock) {
		// Do tear down in an order corresponding to the "reverse" of the order
		// in which we set up things in `addNotify()`
		super.removeNotify()

		val jcef = _jcef
		if (jcef != null) {
			_jcef = null

			contentPane.remove(jcef.component)
			jcef.browser.close(true)
			jcef.client.dispose()
		}
	}

	// --

	private val _uiConfiguration = MutableStateFlow(newUiConfiguration())
	val uiConfiguration: StateFlow<UiConfiguration> get() = _uiConfiguration

	fun updateUiConfiguration() {
		_uiConfiguration.value = newUiConfiguration()
	}

	private fun newUiConfiguration(): UiConfiguration {
		val c = _jcef?.component ?: contentPane
		val s = Size(c.width.dp, c.height.dp)
		return UiConfiguration(
			viewportSize = s,
			darkMode = isDarkMode,
		)
	}

	open val isDarkMode get() = false

	// --

	init {
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK)
	}

	override fun processComponentEvent(e: ComponentEvent) {
		super.processComponentEvent(e)

		if (e.id == ComponentEvent.COMPONENT_RESIZED) {
			updateUiConfiguration()
		}
	}
}
