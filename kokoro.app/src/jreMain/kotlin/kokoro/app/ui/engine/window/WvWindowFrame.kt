package kokoro.app.ui.engine.window

import kokoro.app.AppData
import kokoro.app.Jvm
import kokoro.app.cacheDir
import kokoro.app.logsDir
import kokoro.app.ui.swing.ScopedWindowFrame
import kokoro.app.ui.swing.doOnThreadSwing
import kokoro.internal.DEBUG
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.checkNotNull
import kokoro.jcef.Jcef
import kokoro.jcef.JcefConfig
import kokoro.jcef.JcefStateObserver
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.network.CefRequest
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.GraphicsConfiguration
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowEvent
import java.io.File
import java.net.URI
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(nook::class)
class WvWindowFrame @JvmOverloads constructor(
	@JvmField val handle: WvWindowHandle,
	context: CoroutineContext = EmptyCoroutineContext,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : ScopedWindowFrame(context, DEFAULT_TITLE, gc), WvWindowHandle.Peer {

	companion object {
		init {
			Jcef_globalInit()
		}

		private fun <T> WvWindowBusBinding<*, T>.route(
			window: WvWindow, encoded: ByteArray,
		): Unit = route(window) { bus ->
			WvWindowHandle.PostSerialization.decode(encoded, bus.serialization)
		}
	}

	private var window: WvWindow? = null
	private var isSetUp = false

	@MainThread
	override fun onLaunch() {
		checkNotDisposedPermanently()
		if (window != null) {
			// Reactivates frame if already visible before.
			if (isSetUp) isVisible = true
			return // Already set up or setting up. Skip code below.
		}
		setUpAndShow() // Asserts thread main
	}

	@MainThread
	private fun setUpAndShow() {
		assertThreadMain()
		val h = handle

		val fid = h.windowFactoryId
		val f = checkNotNull(WvWindowFactory.get(fid), or = {
			"No factory registered for window factory ID: $fid"
		})

		val wc = WvContextImpl(h, this)
		val w = f.init(wc) // May throw
		window = w // Set now so that we don't get called again by `onLaunch()`

		wc.scope.launch(Dispatchers.Swing, start = CoroutineStart.UNDISPATCHED) {
			val sizePrefs = w.initSizePrefs() // NOTE: Suspending call

			// Set this first, since on some platforms, changing the resizable
			// state affects the insets of the window.
			if (!sizePrefs.isResizable) isResizable = false

			contentPane.let { c ->
				c.preferredSize = Dimension(sizePrefs.width, sizePrefs.height)
				pack()
				minimumSize = Dimension(
					width - c.width + sizePrefs.minWidth,
					height - c.height + sizePrefs.minHeight,
				)
				c.preferredSize = null // Reset
			}

			// Get the parent's content pane (if any)
			val pc = (w.handle.parent as? WvWindowHandle)
				?.run { peer_ as? WvWindowFrame }
				?.run { contentPane }

			// NOTE: The following also gracefully handles the case for when the
			// location would cause the window bounds to be outside of the
			// screen.
			setLocationRelativeTo(pc)

			isVisible = true
			isSetUp = true // Mark as done!
		}
	}

	class JcefSetup(
		val client: CefClient,
		val browser: CefBrowser,
		val component: Component,
	)

	@PublishedApi @JvmField
	@nook internal var jcef_: JcefSetup? = null

	val jcef: JcefSetup?
		inline get() = jcef_

	@MainThread
	fun loadUrl(url: String) {
		assertThreadMain()
		if (!isDisposedPermanently) {
			jcef_?.let { jcef ->
				jcef.browser.loadURL(url)
				return // Done. Skip code below.
			}
			setUpJcef(url)
		}
	}

	/** @see tearDownJcef */
	@MainThread
	private fun setUpJcef(initUrl: String) {
		assertThreadMain()
		assert({ jcef_ == null })

		// TODO Hook some logging, so that we can detect errors, just like in
		//  the JCEF Maven example app.

		// Must be initialized before all other JCEF interactions, as the
		// following would not only create a new `CefClient` but also initialize
		// `CefApp` and link with the native library. At the time of writing,
		// there is no other way to force that without also creating at least
		// one `CefClient`.
		val client = Jcef.app.createClient()
		client.addRequestHandler(InternalRequestHandler())

		val browser = client.createBrowser(initUrl, false, false)
		// Necessary or the browser component won't respond to the keyboard.
		// Setting up the following once seems to be enough to allow keyboard
		// interaction. See also, https://github.com/chromiumembedded/java-cef/blob/0b8e42e/java/tests/simple/MainFrame.java
		browser.setFocus(true)

		val component = browser.uiComponent
		jcef_ = JcefSetup(client, browser, component)
		contentPane.add(component)
	}

	private class InternalRequestHandler : CefRequestHandlerAdapter() {

		private fun launchUrlExternally(url: String) {
			if (Desktop.isDesktopSupported()) try {
				val desktop = Desktop.getDesktop()
				if (desktop.isSupported(Desktop.Action.BROWSE))
					desktop.browse(URI(url))
			} catch (ex: Throwable) {
				if (DEBUG) throw ex
				ex.printStackTrace()
			}
		}

		override fun onBeforeBrowse(
			browser: CefBrowser?,
			frame: CefFrame?,
			request: CefRequest?,
			user_gesture: Boolean,
			is_redirect: Boolean,
		): Boolean {
			if (frame == null || !frame.isMain || !user_gesture) {
				return false
			}
			if (request != null) {
				launchUrlExternally(request.url)
			}
			return true // Override default behavior
		}

		override fun onOpenURLFromTab(
			browser: CefBrowser?,
			frame: CefFrame?,
			target_url: String?,
			user_gesture: Boolean,
		): Boolean {
			if (target_url != null) {
				launchUrlExternally(target_url)
			}
			return true // Override default behavior
		}
	}

	/** @see setUpJcef */
	@MainThread
	private fun tearDownJcef() {
		assertThreadMain()

		val jcef = jcef_ ?: return
		contentPane.remove(jcef.component)
		jcef_ = null

		// Synchronize on the same lock used by `CefApp.getInstance()`
		synchronized(CefApp::class.java) {
			if (CefApp.getState() < CefAppState.SHUTTING_DOWN) {
				jcef.browser.close(true)
				jcef.client.dispose()
			}
		}
	}

	@MainThread
	override fun onPost(busId: String, payload: ByteArray) {
		assertThreadMain()
		window?.let { w -> (w.getDoOnPost_(busId) ?: return@let).route(w, payload) }
	}

	// --

	init {
		contentPane.addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?): Unit =
				doOnThreadSwing(::dispatchWvWindowResize)
		})
	}

	@MainThread
	private fun dispatchWvWindowResize() {
		assertThreadMain()
		val w = window
		// NOTE: Ignore the dispatch if currently maximized in either axes.
		if (w != null && extendedState and MAXIMIZED_BOTH == 0) {
			val c = contentPane
			w.onResize(c.width, c.height)
		}
	}

	@AnyThread
	override fun processWindowEvent(e: WindowEvent) {
		super.processWindowEvent(e)
		val eid = e.id
		if (eid == WindowEvent.WINDOW_ACTIVATED) doOnThreadSwing(::dispatchWvWindowResume)
		else if (eid == WindowEvent.WINDOW_DEACTIVATED) doOnThreadSwing(::dispatchWvWindowPause)
	}

	@MainThread
	private fun dispatchWvWindowResume() {
		assertThreadMain()
		window?.onResume()
	}

	@MainThread
	private fun dispatchWvWindowPause() {
		assertThreadMain()
		window?.onPause()
	}

	override fun onDisposePermanently() {
		doOnThreadSwing(::dispatchWvWindowDestroy)
		super.onDisposePermanently()
	}

	@MainThread
	private fun dispatchWvWindowDestroy() {
		handle.run {
			detachPeer() // So that `dispose()` isn't called by `close()` below
			close() // Asserts thread main
		}
		tearDownJcef()
		window?.let {
			it.onDestroy() // May throw
			window = null
		}
	}
}

private fun Jcef_globalInit() {
	Jcef.init(JcefConfig(
		cacheDir = File(AppData.Jvm.cacheDir, "jcef"),
		logFile = File(AppData.Jvm.logsDir, "jcef.debug.log").also {
			if (!it.isFile || it.length() > /* 5 MiB */ 5 * 1024 * 1024) {
				it.deleteRecursively()
			}
		},
		stateObservers = listOf(JcefStateObserver(fun(state) {
			if (state < CefAppState.SHUTTING_DOWN) return
			for (w in Window.getWindows()) if (w is WvWindowFrame) {
				w.dispose()
			}
		})),
	))
}
