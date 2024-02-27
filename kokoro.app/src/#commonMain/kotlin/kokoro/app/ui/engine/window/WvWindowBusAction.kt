package kokoro.app.ui.engine.window

fun interface WvWindowBusAction<W : WvWindow, T> {
	fun W.invoke(value: T)
}
