package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.MainThread

expect class WvWindowHandle @nook constructor(
	id: String?,
	windowFactoryId: WvWindowFactoryId,
	parent: WvWindowManager?,
) : WvWindowManager {

	override val id: String?

	@MainThread
	override fun launch()

	@MainThread
	override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean

	// --

	override val isClosed: Boolean

	@MainThread
	override fun onClose()

	// --

	companion object
}
