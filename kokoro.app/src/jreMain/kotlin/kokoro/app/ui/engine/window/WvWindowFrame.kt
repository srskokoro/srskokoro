package kokoro.app.ui.engine.window

import kokoro.app.ui.swing.ScopedWindowFrame
import kokoro.app.ui.swing.doOnThreadSwing
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.checkNotNull
import java.awt.GraphicsConfiguration
import java.awt.event.WindowEvent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(nook::class)
class WvWindowFrame @JvmOverloads constructor(
	@JvmField val handle: WvWindowHandle,
	context: CoroutineContext = EmptyCoroutineContext,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : ScopedWindowFrame(context, DEFAULT_TITLE, gc), WvWindowHandle.Peer {

	private var window: WvWindow? = null

	@MainThread
	override fun onLaunch() {
		if (isDisposedPermanently) return // Already disposed
		initWvWindow() // Asserts thread main
		isVisible = true // Reactivates frame if already visible before
	}

	@MainThread
	private fun initWvWindow() {
		assertThreadMain()
		if (window != null) return // Already initialized

		val h = handle

		val fid = h.windowFactoryId
		val f = checkNotNull(WvWindowFactory.get(fid), or = {
			"No factory registered for window factory ID: $fid"
		})

		val wc = WvContextImpl(h, this)
		val w = f.init(wc) // May throw
		window = w
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
