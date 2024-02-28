package kokoro.app.ui.engine.window

import androidx.collection.MutableScatterMap
import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.UiState
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kotlin.jvm.JvmField

abstract class WvContext {

	abstract val handle: WvWindowHandle

	abstract fun load(url: String)

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
	 * Subsequent calls this method with the same [bus] argument will always
	 * return the same [UiState] instance as before.
	 *
	 * @see WvWindow.onSaveState
	 */
	@MainThread
	fun <T> state(bus: UiBus<T>): UiState<T> {
		assertThreadMain()

		val entry = stateEntries.getOrPut(bus.id) {
			val v = loadOldState(bus) // May throw
			UiState(v, bus)
		}

		@Suppress("UNCHECKED_CAST")
		return entry as UiState<T>
	}

	@MainThread
	protected abstract fun <T> loadOldState(bus: UiBus<T>): T?
}
