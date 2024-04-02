package kokoro.app.ui.engine.window

import kokoro.app.ui.swing.ScopedWindowFrame
import kokoro.app.ui.swing.doOnThreadSwing
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.checkNotNull
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.awt.Dimension
import java.awt.GraphicsConfiguration
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowEvent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(nook::class)
class WvWindowFrame @JvmOverloads constructor(
	@JvmField val handle: WvWindowHandle,
	context: CoroutineContext = EmptyCoroutineContext,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : ScopedWindowFrame(context, DEFAULT_TITLE, gc), WvWindowHandle.Peer {

	companion object {

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
		if (isDisposedPermanently) return // Already disposed
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

		wc.scope.launch(start = CoroutineStart.UNDISPATCHED) {
			val sizePrefs = w.initSizePrefs()
			withContext(Dispatchers.Swing) {
				// Set this first, since on some platforms, changing the
				// resizable state affects the insets of the window.
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

				val pc = (w.handle.parent as? WvWindowHandle)
					?.run { peer_ as? WvWindowFrame }
					?.run { contentPane }

				// NOTE: The following also gracefully handles the case for when
				// the location would cause the window bounds to be outside of
				// the screen.
				setLocationRelativeTo(pc)

				window = w
				isVisible = true

				// Done!
				isSetUp = true
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
		window?.let {
			it.onDestroy()
			window = null
		}
	}
}
