package kokoro.app.ui.engine.window

import kokoro.app.AppData
import kokoro.app.Jvm
import kokoro.app.cacheDir
import kokoro.app.logsDir
import kokoro.app.ui.engine.web.Bom
import kokoro.app.ui.engine.web.PlatformWebRequest
import kokoro.app.ui.engine.web.WebResource
import kokoro.app.ui.engine.web.WebResponse
import kokoro.app.ui.engine.web.WebUri
import kokoro.app.ui.engine.web.WebUriResolver
import kokoro.app.ui.swing.BaseWindowFrame
import kokoro.app.ui.swing.ScopedWindowFrame
import kokoro.app.ui.swing.doOnThreadSwing
import kokoro.app.ui.swing.jcef.toKeyEvent
import kokoro.app.ui.swing.put
import kokoro.app.ui.swing.setLocationBesides
import kokoro.app.ui.swing.usableBounds
import kokoro.internal.DEBUG
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.checkNotNull
import kokoro.internal.coroutines.CancellationSignal
import kokoro.jcef.Jcef
import kokoro.jcef.JcefConfig
import kokoro.jcef.JcefStateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.swing.Swing
import okio.BufferedSource
import okio.ByteString
import okio.buffer
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefCallback
import org.cef.handler.CefFocusHandlerAdapter
import org.cef.handler.CefKeyboardHandler.CefKeyEvent
import org.cef.handler.CefKeyboardHandlerAdapter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.GraphicsConfiguration
import java.awt.KeyboardFocusManager
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File
import java.lang.invoke.VarHandle
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.KeyStroke
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.min

@OptIn(nook::class)
class WvWindowFrame @JvmOverloads constructor(
	@JvmField val handle: WvWindowHandle,
	context: CoroutineContext = EmptyCoroutineContext,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : ScopedWindowFrame(context, DEFAULT_TITLE, gc), WvWindowHandle.Peer {

	companion object {
		private const val CPK_name = "WvWindowFrame.name"

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
			val wur = w.initWebUriResolver() // NOTE: Suspending call
			setUpJcef(wur, w.context.scope)

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
		@JvmField val client: CefClient,
		@JvmField val browser: CefBrowser,
		@JvmField val component: Component,
	)

	@PublishedApi @JvmField
	@nook internal var jcef_: JcefSetup? = null

	val jcef: JcefSetup?
		inline get() = jcef_

	private var devToolsFrame: DevToolsFrame? = null

	private var initUrl: String? = null

	@MainThread
	fun loadUrl(url: String) {
		assertThreadMain()
		jcef_?.let { jcef ->
			jcef.browser.loadURL(url)
			return // Done. Skip code below.
		}
		if (!isDisposedPermanently) initUrl = url
	}

	/** @see tearDownJcef */
	@MainThread
	private fun setUpJcef(wur: WebUriResolver, scope: CoroutineScope) {
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
		client.addRequestHandler(CefRequestHandlerImpl(wur, scope))
		client.addKeyboardHandler(CefKeyboardHandlerImpl(this))
		// TODO Extract into a private class. Name it `CefFocusHandlerImpl`.
		client.addFocusHandler(object : CefFocusHandlerAdapter(), Runnable {
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
				// components; otherwise, focus and traversal of AWT components
				// won't work properly. That is, there must be no focused AWT
				// component while a JCEF browser has focus.
				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
				EventQueue.invokeLater(clearingGlobalFocus)
			}

			// NOTE: The boolean value represents "dispatched" when `true`, and
			// "undispatched" when `false`.
			private val clearingGlobalFocus = object : AtomicBoolean(), Runnable {
				override fun run() = set(false) // Allow redispatch
			}
		})

		val browser = client.createBrowser(initUrl.also { initUrl = null }, false, false)
		val component = browser.uiComponent
		jcef_ = JcefSetup(client, browser, component)
		contentPane.add(component)
	}

	private class DevToolsFrame private constructor(
		private val owner: WvWindowFrame,
		private val devTools: CefBrowser,
		gc: GraphicsConfiguration,
	) : BaseWindowFrame(deriveTitle(owner.title), gc), PropertyChangeListener {

		override fun propertyChange(e: PropertyChangeEvent) {
			title = deriveTitle(e.newValue)
		}

		companion object {

			private fun deriveTitle(ownerTitle: Any?) = "DevTools | $ownerTitle"

			const val DEV_TOOLS_ACTION = "DevTools"

			@MainThread
			fun show(owner: WvWindowFrame) {
				assertThreadMain()
				var fr = owner.devToolsFrame
				if (fr == null) {
					val jcef = owner.jcef ?: return

					val devTools = jcef.browser.devTools
					val gc = owner.graphicsConfiguration
					fr = DevToolsFrame(owner, devTools, gc)
					fr.contentPane.add(devTools.uiComponent)

					owner.addPropertyChangeListener("title", fr)
					owner.devToolsFrame = fr

					val gb = gc.usableBounds
					fr.setSize(min(gb.width, owner.width), min(gb.height, owner.height))
					fr.setLocationBesides(owner, gb)
				}
				// Reactivates frame if already visible before.
				fr.isVisible = true
			}
		}

		override fun dispose(): Unit = doOnThreadSwing {
			val o = owner
			assert({ o.devToolsFrame === this })
			o.devToolsFrame = null
			o.removePropertyChangeListener(this)

			super.dispose()
			devTools.close(true)
		}
	}

	private class CefKeyboardHandlerImpl(private val owner: WvWindowFrame) : CefKeyboardHandlerAdapter() {
		override fun onKeyEvent(browser: CefBrowser?, e: CefKeyEvent?): Boolean {
			if (e != null) run<Unit> {
				val owner = owner
				val jcef = owner.jcef_ ?: return@run
				if (jcef.browser !== browser) return@run
				// Allow Swing/AWT to see and intercept CEF key events.
				// - See also, https://www.magpcss.org/ceforum/viewtopic.php?f=17&t=17305
				val ke = e.toKeyEvent(owner)
				EventQueue.invokeLater {
					// NOTE: By the time we get here, `owner.jcef` may now be
					// null, which is as it should be after being torn down.
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

	private class CefRequestHandlerImpl(
		wur: WebUriResolver,
		scope: CoroutineScope,
	) : CefRequestHandlerAdapter() {
		private val navigationResourceRequestHandler = CefResourceRequestHandlerImpl(isNavigation = true, wur, scope)
		private val generalResourceRequestHandler = CefResourceRequestHandlerImpl(isNavigation = false, wur, scope)

		override fun getResourceRequestHandler(
			browser: CefBrowser?, frame: CefFrame?, request: CefRequest?,
			isNavigation: Boolean, isDownload: Boolean,
			requestInitiator: String?, disableDefaultHandling: BoolRef?,
		): CefResourceRequestHandler =
			if (isNavigation) navigationResourceRequestHandler
			else generalResourceRequestHandler

		// --

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
				// TIP: See also `Sec-Fetch-User` request header -- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-User
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

	private class CefResourceRequestHandlerImpl(
		private val isNavigation: Boolean,
		private val wur: WebUriResolver,
		private val scope: CoroutineScope,
	) : CefResourceRequestHandlerAdapter() {
		override fun getResourceHandler(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): CefResourceHandler? {
			if (request != null) {
				val uri = WebUri(request.url)
				val h = wur.resolve(uri)
				if (h != null) return CefResourceHandlerImpl(
					PlatformWebRequest(request, uri),
					isNavigation = isNavigation,
					h, scope,
				)
			}
			return null
		}
	}

	private class CefResourceHandlerImpl(
		private val platformRequest: PlatformWebRequest,
		private val isNavigation: Boolean,
		private val handler: WebResource,
		private val scope: CoroutineScope,
	) : CefResourceHandler {
		private var responseContentExhausted: Boolean = false // Guarded by `responseContent`
		private var responseContentBom: ByteString? = null // Unguarded
		private var responseContent: BufferedSource? = null
		private var response: WebResponse? = null

		suspend fun initWebResponse() {
			val r = handler.apply(platformRequest) // NOTE: Suspending call
			response = r
			responseContent = r.content.buffer()
		}

		override fun processRequest(request: CefRequest?, callback: CefCallback): Boolean {
			@OptIn(ExperimentalCoroutinesApi::class)
			scope.launch(Dispatchers.IO, start = CoroutineStart.ATOMIC) {
				try {
					initWebResponse()
					VarHandle.releaseFence()
					// ^ NOTE: We don't trust that the call below (or its
					// internals) won't be reordered before the code above.
					callback.Continue()
					return@launch // Skip code below
				} catch (ex: Throwable) {
					callback.cancel()
					throw ex
				}
			}
			return true // Handled
		}

		override fun getResponseHeaders(out: CefResponse, contentLengthOut: IntRef, redirectUrl: StringRef?) {
			val r = this.response!!

			out.status = r.status
			out.setHeaderMap(r.headers)

			var contentLength = r.contentLength
			var contentType = r.mimeType
			if (contentType != null) {
				val charset = r.charset
				if (charset != null) run<Unit> {
					// NOTE: For the MIME types listed below, CEF currently
					// doesn't support an explicit `charset` parameter for
					// custom responses. The following mitigates this issue by
					// automatically supplying a BOM.
					//
					// See also,
					// - https://www.magpcss.org/ceforum/viewtopic.php?f=10&t=894
					// - https://github.com/cefsharp/CefSharp/issues/689
					if (isNavigation) when (contentType) {
						"application/json",
						"application/xhtml+xml",
						"application/xml",
						"text/css",
						"text/html",
						"text/javascript",
						"text/plain",
						-> Bom.forMediaCharset(charset)?.let { bom ->
							// NOTE: By default, JCEF uses "ISO-8859-1" (which
							// is a superset of "US-ASCII"). Also, "US-ASCII" is
							// the default charset for the "text" MIME type --
							// see, https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#structure_of_a_mime_type
							// - See also, https://github.com/cefsharp/CefSharp/issues/689#issuecomment-67086264
							val bom_n = bom.size
							if (bom_n > 0) {
								this.responseContentBom = bom
								if (contentLength >= 0)
									contentLength += bom_n
							}
							return@run // Skip code below
						}
					}
					contentType = "$contentType; charset=$charset"
				}
				// NOTE: Even if we set a "Content-Type" header, the following
				// `CefResponse.setMimeType()` configuration will overwrite it.
				// Furthermore, CEF relies on `CefResponse.setMimeType()`, so
				// setting only the "Content-Type" header will still have no
				// effect even if we omit the `CefResponse.setMimeType()` call.
				out.mimeType = contentType
			}

			if (contentLength > 0) {
				if (contentLength <= Int.MAX_VALUE) {
					contentLengthOut.set(contentLength.toInt())
				} else {
					contentLengthOut.set(-1)
					out.setHeaderByName(
						"content-length",
						contentLength.toString(),
						/* overwrite = */ true,
					)
				}
			} else {
				// NOTE: Even if `contentLength` is zero, set this to `-1`, so
				// as to ensure `readResponse()` is still called.
				contentLengthOut.set(-1)
				if (contentLength == 0L) {
					out.setHeaderByName(
						"content-length", "0",
						/* overwrite = */ true,
					)
				}
			}
		}

		override fun readResponse(dataOut: ByteArray, bytesToRead: Int, bytesRead: IntRef, callback: CefCallback): Boolean {
			val source = responseContent!!

			synchronized(source) {
				assert({ source.buffer.isOpen }) // NOTE: `source.buffer` never really closes.
				val transferred = source.buffer.read(dataOut, 0, bytesToRead)
				if (transferred > 0) {
					bytesRead.set(transferred)
					return true // Not done yet
				} else if (!responseContentExhausted) {
					bytesRead.set(0)
					// Skip below
				} else {
					source.close()
					return false // Done
				}
			}

			val bom = responseContentBom
			if (bom != null) responseContentBom = null

			@OptIn(ExperimentalCoroutinesApi::class)
			scope.launch(Dispatchers.IO, start = CoroutineStart.ATOMIC) {
				try {
					runInterruptible {
						synchronized(source) {
							if (!source.isOpen) {
								responseContentExhausted = true
								throw CancellationSignal()
							}

							if (bom != null) {
								val bom_n = bom.size
								val b = source.buffer
								b.write(bom, 0, bom_n) // Prepend BOM
								if (
									source.request(bom_n * 2L) &&
									b.rangeEquals(bom_n.toLong(), bom, 0, bom_n)
								) {
									// BOM was already present
									b.skip(bom_n.toLong())
								}
							}

							if (!source.request(bytesToRead.toLong())) {
								// Already exhausted
								responseContentExhausted = true
							}
						}
					}
					callback.Continue()
					return@launch // Skip code below
				} catch (ex: Throwable) {
					callback.cancel()
					throw ex
				}
			}
			return true // Not done yet
		}

		override fun cancel() {
			responseContent?.let {
				synchronized(it) {
					it.close()
				}
			}
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
				devToolsFrame?.dispose()
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
		val root = rootPane
		root.contentPane.addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?): Unit =
				doOnThreadSwing(::dispatchWvWindowResize)
		})

		val rootActionMap = root.actionMap
		val rootInputMap = root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

		rootInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), DevToolsFrame.DEV_TOOLS_ACTION)
		rootActionMap.put(DevToolsFrame.DEV_TOOLS_ACTION) { DevToolsFrame.show(this) }
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

	override fun getName(): String {
		val rootPane = rootPane
		var name = rootPane.getClientProperty(CPK_name) as String?
		if (name == null) {
			val h = handle
			name = "WvWindow[${h.id_}:${h.windowFactoryId}]"
			rootPane.putClientProperty(CPK_name, name)
		}
		return name
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
