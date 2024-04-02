package kokoro.app.ui.engine.window

import androidx.annotation.EmptySuper
import androidx.annotation.IntDef
import androidx.collection.MutableScatterMap
import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.check
import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.JvmField

@OptIn(nook::class)
abstract class WvWindow(@JvmField val context: WvContext) {

	val handle: WvWindowHandle inline get() = context.handle
	val scope: CoroutineScope inline get() = context.scope

	@Suppress("NOTHING_TO_INLINE")
	inline fun loadUrl(url: String) = context.load(url = url)

	@Suppress("NOTHING_TO_INLINE")
	inline fun finish() = context.finish()

	// --

	@MainThread
	open fun initSizePrefs() = SizePrefs(SizePrefs.FLAG_RESIZABLE, SizeRule.SQUARE)

	data class SizePrefs(
		@Flags val flags: Int,
		val rule: SizeRule,
	) {
		companion object {

			const val FLAG_RESIZABLE = 1 shl 0

			/**
			 * Remembers any resizing done by the user for windows of the same
			 * [WvWindowFactoryId]. Ignored if [FLAG_RESIZABLE] isn't set.
			 *
			 * Launching a new window that has the same [WvWindowFactoryId] as
			 * another window that has already been launched, would cause the
			 * newly launched window's size to be the same as the other window's
			 * current size.
			 */
			const val FLAG_REMEMBER_USER_SIZE = 1 shl 1
		}

		@IntDef(
			flag = true,
			value = [
				FLAG_RESIZABLE,
				FLAG_REMEMBER_USER_SIZE,
			]
		)
		annotation class Flags
	}

	data class SizeRule(
		val initWidth: Int, val initHeight: Int,
		val minWidth: Int, val minHeight: Int,
	) {
		companion object {
			val SMALL = SizeRule(
				initWidth = 360, initHeight = 240,
				minWidth = 360, minHeight = 240,
			)
			val SQUARE = SizeRule(
				initWidth = 600, initHeight = 600,
				minWidth = 480, minHeight = 480,
			)
			val WIDE = SQUARE.run { copy(initWidth = initWidth * 2) }
		}
	}

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
