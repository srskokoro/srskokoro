package kokoro.app.ui.engine.window

import androidx.collection.MutableScatterMap
import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.UiState
import kokoro.app.ui.engine.web.WebResource
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.collections.computeIfAbsent
import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.JvmField

abstract class WvContext(
	@JvmField val handle: WvWindowHandle,
	@JvmField val scope: CoroutineScope,
) {
	@get:MainThread
	@set:MainThread
	abstract var title: CharSequence?

	@MainThread
	abstract fun load(url: String)

	@MainThread
	abstract fun finish()

	// --

	/** WARNING: Must only be accessed (and modified) from the main thread. */
	@JvmField protected val stateEntries = MutableScatterMap<String, UiState<*>>()

	/**
	 * Provides a [UiState] (associated with the given [bus]) which can be used
	 * to store saved states. There is a [UiState] for every [bus.id][UiBus.id]
	 * given to this method.
	 *
	 * Whenever the application's state needs saving, the "current" [value][UiState.value]
	 * of the returned [UiState] is serialized and saved along with the
	 * application state. On Android, this is triggered by the
	 * `Activity.onSaveInstanceState()` method.
	 *
	 * If there was a previously saved state for the the given [bus] and the
	 * [UiState] has not been created yet, the saved state is first loaded as
	 * the initial value of the [UiState] that would be returned by this method.
	 *
	 * Subsequent calls to this method with the same [bus] argument will always
	 * return the same [UiState] instance as before.
	 *
	 * @see WvWindow.onSaveState
	 */
	@MainThread
	fun <T> state(bus: UiBus<T>): UiState<T> {
		assertThreadMain()

		val entry = stateEntries.computeIfAbsent(bus.id, onExisting = { id, entry ->
			assert({ entry.bus == bus }, or = { "Inconsistent bus usage.\n- ID: $id" })
		}) { _ ->
			val v = loadOldState(bus) // May throw
			UiState(v, bus)
		}

		@Suppress("UNCHECKED_CAST")
		return entry as UiState<T>
	}

	@MainThread
	protected abstract fun <T> loadOldState(bus: UiBus<T>): T?

	// --

	abstract fun initPlatformJs(): WebResource
}
