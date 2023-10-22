package kokoro.app.ui

import androidx.compose.runtime.*
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.dp
import kokoro.app.ui.redwood.RedwoodWindowFrame
import kokoro.jcef.Jcef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.AWTEvent
import java.awt.Component
import java.awt.EventQueue
import java.awt.GraphicsConfiguration
import java.awt.event.ComponentEvent

class AppWindowFrame(
	private val mainScope: CoroutineScope,
	private val spec: WindowSpec, args: List<Any?> = emptyList(),
	gc: GraphicsConfiguration? = null,
) : RedwoodWindowFrame(DEFAULT_TITLE, gc), WindowHost, AppLafListener {

	companion object {
		private const val DEFAULT_TITLE = ""

		init {
			// NOTE: LAFs need to be set up before window creation. The static
			// initializer for the class is therefore the best place to do this.
			ensureAppLaf()
		}
	}

	private val _state: WindowStateImpl
	val state: WindowState get() = _state

	init {
		val state = WindowStateImpl()
		_state = state
		spec.onNewArgs(state, args)
	}

	private inner class WindowStateImpl : AbstractWindowState() {
		var _title by mutableStateOf(DEFAULT_TITLE)
		override var title: String
			get() = _title
			set(value) {
				_title = value
				if (EventQueue.isDispatchThread()) {
					super@AppWindowFrame.setTitle(value)
				} else EventQueue.invokeLater {
					super@AppWindowFrame.setTitle(value)
				}
			}
	}

	override fun setTitle(title: String?) {
		_state._title = title ?: ""
		super.setTitle(title)
	}

	// --

	class JcefState(
		val client: CefClient,
		val browser: CefBrowser,
		val component: Component,
	)

	private var _jcef: JcefState? = null
	val jcef get() = _jcef

	override fun addNotify() {
		if (_jcef == null) {
			val client = Jcef.app.createClient()
			client.addLoadHandler(object : CefLoadHandlerAdapter() {
				override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
					// TODO Maybe use `DOMContentLoaded` instead?
					//  See, https://magpcss.org/ceforum/viewtopic.php?t=10277

					// TODO Set up
				}
			})

			// TODO Properly set up things
			val browser = client.createBrowser("", false, false)

			val component = browser.uiComponent
			contentPane.add(component)

			_jcef = JcefState(client, browser, component)
		}

		super.addNotify()
	}

	override fun removeNotify() {
		// Do tear down in an order corresponding to the "reverse" of the order
		// in which we set up things in `addNotify()`
		super.removeNotify()

		_jcef?.let { jcef ->
			_jcef = null

			contentPane.remove(jcef.component)
			jcef.browser.close(true)
			jcef.client.dispose()
		}
	}

	// --

	private val _uiConfiguration = MutableStateFlow(newUiConfiguration())
	val uiConfiguration: StateFlow<UiConfiguration> get() = _uiConfiguration

	private fun updateUiConfiguration() {
		_uiConfiguration.value = newUiConfiguration()
	}

	private fun newUiConfiguration(): UiConfiguration {
		val c = _jcef?.component ?: contentPane
		val s = Size(c.width.dp, c.height.dp)
		return UiConfiguration(
			viewportSize = s,
			darkMode = isDarkAppLaf,
		)
	}

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

	override fun onAppLafUpdated() {
		updateUiConfiguration()
	}
}
