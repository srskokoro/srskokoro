package kokoro.app.ui.engine.window

import kokoro.app.AppData
import kokoro.app.Jvm
import kokoro.app.cacheDir
import kokoro.app.logsDir
import kokoro.app.ui.engine.web.WebUriResolver
import kokoro.app.ui.engine.window.jcef.CefFocusHandlerImpl
import kokoro.app.ui.engine.window.jcef.CefKeyboardHandlerImpl
import kokoro.app.ui.engine.window.jcef.CefRequestHandlerImpl
import kokoro.app.ui.swing.BaseWindowFrame
import kokoro.app.ui.swing.ScopedWindowFrame
import kokoro.app.ui.swing.doOnThreadSwing
import kokoro.app.ui.swing.put
import kokoro.app.ui.swing.setLocationBesides
import kokoro.app.ui.swing.usableBounds
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.checkNotNull
import kokoro.jcef.Jcef
import kokoro.jcef.JcefConfig
import kokoro.jcef.JcefStateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import okio.buffer
import okio.sink
import okio.source
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.Component
import java.awt.Dimension
import java.awt.GraphicsConfiguration
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File
import java.nio.file.FileSystemException
import javax.swing.JComponent
import javax.swing.KeyStroke
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.min

@OptIn(nook::class)
class WvWindowFrame @JvmOverloads @nook constructor(
	@JvmField val handle: WvWindowHandle,
	context: CoroutineContext = EmptyCoroutineContext,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : ScopedWindowFrame(context, DEFAULT_TITLE, gc), WvWindowHandle.Peer {

	init {
		assert({ handle.peer_ == null })
	}

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
	@nook override fun onLaunch() {
		assertNotDisposedPermanently()
		if (window != null) {
			if (isSetUp) {
				// Reactivates frame if already visible before.
				// - Throws if already disposed permanently.
				isVisible = true
			}
			return // Already set up or setting up. Skip code below.
		}
		setUpAndShow() // Asserts thread main
	}

	@MainThread
	private fun setUpAndShow() {
		assertThreadMain()
		val h = handle

		val fid = h.getIdOrThrow().factoryId
		val f = checkNotNull(WvWindowFactory.get(fid), or = {
			"No factory registered for window factory ID: $fid"
		})

		val wc = WvContextImpl(h, this)
		val w = f.init(wc, true) // May throw
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

	@Volatile
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
		client.addRequestHandler(CefRequestHandlerImpl(this, wur, scope))
		client.addKeyboardHandler(CefKeyboardHandlerImpl(this))
		client.addFocusHandler(CefFocusHandlerImpl())

		val browser = client.createBrowser(initUrl.also { initUrl = null }, false, false)
		val component = browser.uiComponent
		jcef_ = JcefSetup(client, browser, component)
		contentPane.add(component)
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun isMainBrowser(browser: CefBrowser?): Boolean {
		val jcef = jcef_
		return jcef != null && jcef.browser === browser
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

			private const val PROP_title = "title"

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

					owner.devToolsFrame = fr
					owner.addPropertyChangeListener(PROP_title, fr)

					val gb = gc.usableBounds
					fr.setSize(min(gb.width, owner.width), min(gb.height, owner.height))
					fr.setLocationBesides(owner, gb)
				}
				// Reactivates the window frame if already visible before.
				// - The window will be made displayable if not already so, and
				// `super.dispose()` must be called to undo it.
				// - Only once the window has been made displayable would the
				// devtools component also be made displayable.
				// - We must undo all of the above in reverse order on window
				// disposal.
				fr.isVisible = true
			}
		}

		override fun dispose(): Unit = doOnThreadSwing {
			devTools.close(true)
			super.dispose()

			val o = owner
			o.removePropertyChangeListener(PROP_title, this)
			assert({ o.devToolsFrame === this })
			o.devToolsFrame = null
		}
	}

	/** @see setUpJcef */
	@MainThread
	private fun tearDownJcef() {
		assertThreadMain()

		val jcef = jcef_ ?: return
		contentPane.remove(jcef.component)
		jcef_ = null

		// We don't care if the following check happens in a race. Locking might
		// entail a deadlock, thus we avoid that.
		if (CefApp.getState() < CefAppState.SHUTTING_DOWN) {
			devToolsFrame?.dispose()
			jcef.browser.close(true)
			jcef.client.dispose()
		}
	}

	@MainThread
	@nook override fun onPost(busId: String, payload: ByteArray) {
		assertNotDisposedPermanently()
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
		window?.onDestroy(true) // May throw
	}

	override fun getName(): String {
		val rootPane = rootPane
		var name = rootPane.getClientProperty(CPK_name) as String?
		if (name == null) {
			val h = handle
			name = "WvWindow[${h.id}]"
			rootPane.putClientProperty(CPK_name, name)
		}
		return name
	}
}

private fun Jcef_globalInit() {
	Jcef.init(JcefConfig(
		persistData = false,
		cacheDir = File(AppData.Jvm.cacheDir, "jcef"),
		logFile = Jcef_globalInit_logFile(),
		stateObservers = listOf(JcefStateObserver(fun(state) {
			if (state < CefAppState.SHUTTING_DOWN) return
			for (w in Window.getWindows()) if (w is WvWindowFrame) {
				w.dispose()
			}
		})),
	))
}

private fun Jcef_globalInit_logFile(): File {
	val dir = AppData.Jvm.logsDir
	val file = File(dir, "jcef.debug.log")
	val tmp = File(dir, "jcef.debug.log.tmp")
	if (!file.isFile) {
		if (!file.deleteRecursively())
			throw FileSystemException(file.path, null, "Deletion failed.")
		if (tmp.exists() && !tmp.renameTo(file))
			throw FileSystemException(tmp.path, file.path, "Rename failed.")
	} else run {
		if (tmp.exists() && !tmp.delete())
			throw FileSystemException(tmp.path, null, "Deletion failed.")

		val length = file.length() - /* 5 MiB */ 5 * 1024 * 1024
		if (length <= 0) return@run

		// Truncate the file by removing initial bytes
		file.source().buffer().use { src ->
			src.skip(length)
			// Skip the first, possibly broken, line.
			src.buffer.indexOf('\n'.code.toByte()).let {
				src.skip(it + 1)
			}
			val out = tmp.outputStream()
			out.sink().buffer().use {
				it.writeAll(src)
				it.flush()
				out.fd.sync()
			}
		}
		if (!file.delete() || !tmp.renameTo(file))
			throw FileSystemException(tmp.path, file.path, "Rename failed.")
	}
	return file
}
