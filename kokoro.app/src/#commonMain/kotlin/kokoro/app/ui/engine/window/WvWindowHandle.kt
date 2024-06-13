package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kotlinx.coroutines.Job

expect class WvWindowHandle @nook internal constructor(
	id: WvWindowId?,
	parent: WvWindowManager?,
) : WvWindowManager {

	override val closeJob: Job

	@MainThread
	override fun onClose()

	companion object {

		@AnyThread
		fun closed(): WvWindowHandle
	}

	@MainThread
	override fun launchOrReject(): Boolean

	@MainThread
	override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean
}
