package kokoro.app.ui.engine.window

import androidx.compose.runtime.*
import kokoro.app.ui.engine.WvSetup
import kokoro.internal.serialization.NullableNothingSerializer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

// TODO! Remove `kokoro.app.ui.WindowSpec` as it has been replaced already
abstract class WindowCore<A : WindowArgs> {

	abstract class NoArgs : WindowCore<Nothing?>() {

		final override fun onLoad(newArgs: Nothing?) = super.onLoad(newArgs)
	}

	// --

	internal var _windowId: WindowId<A>? = null
	val windowId: WindowId<A> get() = _windowId ?: throw E_NotYetInitialized()

	private val _title_state = mutableStateOf<String?>(null)
	internal var _title_hook: (String?) -> Unit = {}
	var title: String?
		set(value) {
			_title_hook.invoke(value)
			_title_state.value = value
		}
		get() = _title_state.value

	private val _args_state = mutableStateOf<A?>(null)
	val oldArgs: A? get() = _args_state.value
	var args: A
		set(value) {
			_args_state.value = value
		}
		get() {
			@Suppress("UNCHECKED_CAST")
			return _args_state.value as A
		}

	// --

	open fun onLoad(newArgs: A) {
		args = newArgs
		onLoad()
	}

	open fun onLoad() = Unit

	abstract fun wv(): WvSetup

	@Composable
	abstract fun Content()

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun <T> post(statusBus: StatusBus<T>, value: T) = status(statusBus, value)

	fun <T> status(statusBus: StatusBus<T>, value: T) {
		status(statusBus).tryEmit(value)
	}

	fun <T> status(statusBus: StatusBus<T>): MutableSharedFlow<T> {
		TODO("Not yet implemented")
	}

	abstract class StatusBus<T> @PublishedApi internal constructor(@JvmField val busId: Int) {
		abstract fun SerializersModule.valueSerializer(): KSerializer<T>?

		companion object {
			@Suppress("NOTHING_TO_INLINE")
			@JvmName("invoke_nothing")
			inline operator fun invoke(busId: Int): StatusBus<Nothing?> = invoke(busId = busId) { NullableNothingSerializer() }

			inline operator fun <reified T> invoke(busId: Int): StatusBus<T> = invoke(busId = busId) { serializer<T>() }

			inline operator fun <T> invoke(busId: Int, crossinline serializer: SerializersModule.() -> KSerializer<T>?) = object : StatusBus<T>(busId) {
				override fun SerializersModule.valueSerializer() = serializer()
			}
		}
	}
}

private fun E_NotYetInitialized() = IllegalStateException("Not yet initialized")
