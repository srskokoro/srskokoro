package kokoro.app.ui.engine.window

import androidx.annotation.EmptySuper
import androidx.collection.MutableScatterMap
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.collections.initWithAssert
import kotlin.jvm.JvmField

@Suppress("MemberVisibilityCanBePrivate")
@OptIn(nook::class)
abstract class WvWindow(@JvmField val context: WvContext) {

	val handle: WvWindowHandle inline get() = context.handle

	@Suppress("NOTHING_TO_INLINE")
	inline fun loadUrl(url: String) = context.load(url = url)

	@Suppress("NOTHING_TO_INLINE")
	inline fun finish() = context.finish()

	// --

	@EmptySuper
	@MainThread
	open fun onResume() = Unit

	@EmptySuper
	@MainThread
	open fun onPause() = Unit

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
	@PublishedApi @nook internal fun <W : WvWindow, T> doOnPost_(bus: WvWindowBus<T>, action: WvWindowBusAction<W, T>) {
		assertThreadMain()
		doOnPostMap.initWithAssert(bus.id, WvWindowBusBinding(bus, action), or = {
			"Bus already bound. ID: ${bus.id}"
		})
	}

	@MainThread
	@PublishedApi @nook internal fun <W : WvWindow, T> setDoOnPost_(bus: WvWindowBus<T>, action: WvWindowBusAction<W, T>) {
		assertThreadMain()
		doOnPostMap[bus.id] = WvWindowBusBinding(bus, action)
	}

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	inline fun <W : WvWindow, T> W.doOnPost(bus: WvWindowBus<T>, action: WvWindowBusAction<W, T>) =
		doOnPost_(bus, action)

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	inline fun <W : WvWindow, T> W.setDoOnPost(bus: WvWindowBus<T>, action: WvWindowBusAction<W, T>) =
		setDoOnPost_(bus, action)

	@MainThread
	fun <T> unsetDoOnPost(bus: WvWindowBus<T>) {
		assertThreadMain()
		doOnPostMap.remove(bus.id)
	}
}
