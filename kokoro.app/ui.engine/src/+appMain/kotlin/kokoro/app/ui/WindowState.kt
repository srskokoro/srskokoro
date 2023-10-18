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

fun WindowState(): WindowState = WindowStateImpl()

interface WindowState {

	val args: List<Any?>
	fun updateArgs(args: List<Any?>)

	fun <T> getExtra(): T
	fun setExtra(value: Any?)

	var title: String

	companion object
}

private class WindowStateImpl : WindowState {

	override var args by mutableStateOf(emptyList<Any?>())

	override fun updateArgs(args: List<Any?>) {
		this.args = args
	}

	var extra by mutableStateOf<Any?>(null)

	override fun <T> getExtra(): T {
		@Suppress("UNCHECKED_CAST")
		return extra as T
	}

	override fun setExtra(value: Any?) {
		extra = value
	}

	override var title by mutableStateOf("")
}
