package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread

actual sealed class WvWindowHandleBasis @AnyThread actual constructor(
	id: String?,
	windowFactoryId: WvWindowFactoryId,
	parent: WvWindowHandle?,
) {
	actual val id: String? get() = TODO("Not yet implemented")
	actual val windowFactoryId: WvWindowFactoryId get() = TODO("Not yet implemented")

	@nook actual var parent_: WvWindowHandle? = parent

	@MainThread
	actual fun launch() {
		TODO("Not yet implemented")
	}

	@MainThread
	actual fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		TODO("Not yet implemented")
	}

	@MainThread
	protected actual fun onClose() {
		TODO("Not yet implemented")
	}

	actual val isClosed: Boolean get() = TODO("Not yet implemented")
}
