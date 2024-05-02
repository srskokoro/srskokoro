package kokoro.app.ui.engine.window

import androidx.annotation.EmptySuper
import androidx.collection.MutableScatterMap
import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.web.WebRequestHandler
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.check
import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.JvmField

@OptIn(nook::class)
abstract class WvWindow(@JvmField val context: WvContext) {

	companion object {
		val DEFAULT_WEB_REQUEST_HANDLER inline get() = WebRequestHandler.NULL
	}

	val handle: WvWindowHandle inline get() = context.handle
	val scope: CoroutineScope inline get() = context.scope

	@MainThread
	open fun initWebRequestHandler(): WebRequestHandler = DEFAULT_WEB_REQUEST_HANDLER

	@Suppress("NOTHING_TO_INLINE")
	inline fun loadUrl(url: String) = context.load(url = url)

	@Suppress("NOTHING_TO_INLINE")
	inline fun finish() = context.finish()

	// --

	/**
	 * @see initSizePrefs
	 */
	data class SizePrefs(
		val isResizable: Boolean = true,
		val width: Int, val height: Int,
		val minWidth: Int, val minHeight: Int,
	) {
		companion object {
			val SMALL = SizePrefs(
				width = 360, height = 240,
				minWidth = 360, minHeight = 240,
			)
			val SQUARE = SizePrefs(
				width = 600, height = 600,
				minWidth = 480, minHeight = 480,
			)
			val WIDE = SQUARE.run { copy(width = width * 2) }
		}
	}

	/**
	 * @see onResize
	 */
	@MainThread
	open suspend fun initSizePrefs(): SizePrefs = SizePrefs.SQUARE

	/**
	 * @see initSizePrefs
	 */
	@EmptySuper
	@MainThread
	open fun onResize(newWidth: Int, newHeight: Int) = Unit

	// --

	@EmptySuper
	@MainThread
	open fun onResume() = Unit

	@EmptySuper
	@MainThread
	open fun onPause() = Unit

	/**
	 * @see WvContext.state
	 */
	@EmptySuper
	@MainThread
	open fun onSaveState() = Unit

	/**
	 * NOTE: By the time this is called, [handle] is expected to be already
	 * [closed][WvWindowHandle.isClosed] and invalid.
	 */
	@EmptySuper
	@MainThread
	open fun onDestroy() = Unit

	// --

	/** WARNING: Must only be accessed (and modified) from the main thread. */
	private val doOnPostMap = MutableScatterMap<String, WvWindowBusBinding<*, *>>()

	@MainThread
	@PublishedApi @nook internal fun getDoOnPost_(busId: String): WvWindowBusBinding<*, *>? {
		assertThreadMain()
		return doOnPostMap[busId]
	}

	@MainThread
	@PublishedApi @nook internal fun <W : WvWindow, T> doOnPost_(bus: UiBus<T>, action: WvWindowBusAction<W, T>) {
		assertThreadMain()
		doOnPostMap.compute(bus.id) { k, v ->
			check(v == null, or = { "Bus ID already bound: $k" })
			WvWindowBusBinding(bus, action)
		}
	}

	@MainThread
	@PublishedApi @nook internal fun <W : WvWindow, T> setDoOnPost_(bus: UiBus<T>, action: WvWindowBusAction<W, T>) {
		assertThreadMain()
		doOnPostMap[bus.id] = WvWindowBusBinding(bus, action)
	}

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	inline fun <W : WvWindow, T> W.doOnPost(bus: UiBus<T>, action: WvWindowBusAction<W, T>) =
		doOnPost_(bus, action)

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	inline fun <W : WvWindow, T> W.setDoOnPost(bus: UiBus<T>, action: WvWindowBusAction<W, T>) =
		setDoOnPost_(bus, action)

	@MainThread
	fun <T> unsetDoOnPost(bus: UiBus<T>) {
		assertThreadMain()
		doOnPostMap.remove(bus.id)
	}
}
