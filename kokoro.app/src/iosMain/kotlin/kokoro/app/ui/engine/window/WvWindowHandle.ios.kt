package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus

internal actual class WvWindowHandleImpl(parent: WvWindowHandle?) : WvWindowHandle(parent) {

	actual override fun launch(windowFactoryId: String): WvWindowHandle {
		TODO("Not yet implemented")
	}

	actual override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		TODO("Not yet implemented")
	}

	actual override fun onClose() {
		TODO("Not yet implemented")
	}

	actual companion object
}
