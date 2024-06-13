package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kotlinx.coroutines.Job

@OptIn(nook::class)
actual class WvWindowHandle @nook internal actual constructor(
	id: WvWindowId?,
	parent: WvWindowManager?,
) : WvWindowManager(id, parent) {

	actual override val closeJob: Job
		get() = TODO("Not yet implemented")

	@MainThread
	actual override fun onClose() {
		TODO("Not yet implemented")
	}

	actual companion object {

		@AnyThread
		actual fun closed(): WvWindowHandle = TODO("Not yet implemented")
	}

	@MainThread
	actual override fun launchOrReject(): Boolean {
		TODO("Not yet implemented")
	}

	@MainThread
	actual override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		TODO("Not yet implemented")
	}
}
