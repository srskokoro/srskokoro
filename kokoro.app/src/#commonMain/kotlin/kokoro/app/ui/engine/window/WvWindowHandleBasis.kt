package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread

expect sealed class WvWindowHandleBasis @AnyThread constructor(
	id: String?,
	windowFactoryId: WvWindowFactoryId,
	parent: WvWindowHandle?,
) {
	val id: String?
	val windowFactoryId: WvWindowFactoryId

	/** WARNING: Must only be modified from the main thread. */
	@nook var parent_: WvWindowHandle?

	@MainThread
	fun launch()

	@MainThread
	fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean

	@MainThread
	protected fun onClose()

	val isClosed: Boolean
}
