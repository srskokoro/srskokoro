package kokoro.app.ui.engine.window

class WvWindowBusBinding<W : WvWindow, T>(
	val bus: WvWindowBus<T>,
	val action: WvWindowBusAction<W, T>,
) {
	inline fun route(window: WvWindow, transport: (WvWindowBus<T>) -> T) {
		@Suppress("UNCHECKED_CAST")
		with(action) {
			(window as W).invoke(transport(bus))
		}
	}
}
