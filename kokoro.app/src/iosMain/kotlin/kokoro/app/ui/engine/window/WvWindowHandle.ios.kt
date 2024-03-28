package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.MainThread

@OptIn(nook::class)
actual class WvWindowHandle @nook actual constructor(
	id: String?,
	windowFactoryId: WvWindowFactoryId,
	parent: WvWindowManager?,
) : WvWindowManager(windowFactoryId, parent) {

	actual override val id: String?
		get() = TODO("Not yet implemented")

	@MainThread
	actual override fun launch() {
		TODO("Not yet implemented")
	}

	@MainThread
	actual override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		TODO("Not yet implemented")
	}

	// --

	actual override val isClosed: Boolean
		get() = TODO("Not yet implemented")

	@MainThread
	actual override fun onClose() {
		TODO("Not yet implemented")
	}

	// --

	actual companion object
}
