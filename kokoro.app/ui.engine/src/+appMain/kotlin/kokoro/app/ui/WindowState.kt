package kokoro.app.ui

import androidx.compose.runtime.*

val LocalWindowState = staticCompositionLocalOf<WindowState> {
	throw AssertionError("Value was not provided!")
}

inline val WindowState.Companion.current
	@Composable
	@ReadOnlyComposable
	get() = LocalWindowState.current

inline val WindowState.Companion.args
	@Composable get() = current.args

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun <T> WindowState.Companion.getExtra() =
	current.getExtra<T>()

// --

interface WindowState {

	val args: List<Any?>
	fun updateArgs(args: List<Any?>)

	fun <T> getExtra(): T
	fun setExtra(value: Any?)

	var title: String

	companion object
}

abstract class AbstractWindowState : WindowState {
	private var _args = mutableStateOf(emptyList<Any?>())
	override val args get() = _args.value

	override fun updateArgs(args: List<Any?>) {
		_args.value = args
	}

	private var _extra = mutableStateOf<Any?>(null)

	override fun <T> getExtra(): T {
		@Suppress("UNCHECKED_CAST")
		return _extra.value as T
	}

	override fun setExtra(value: Any?) {
		_extra.value = value
	}
}
