package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus

class WvWindowBusBinding<W : WvWindow, T>(
	val bus: UiBus<T>,
	val action: WvWindowBusAction<W, T>,
) {
	inline fun route(window: WvWindow, transport: (UiBus<T>) -> T) {
		@Suppress("UNCHECKED_CAST")
		with(action) {
			(window as W).invoke(transport(bus))
		}
	}
}
