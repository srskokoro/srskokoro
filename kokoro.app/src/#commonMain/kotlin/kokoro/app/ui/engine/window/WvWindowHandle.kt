package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.MainThread
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.selects.SelectClause0

expect class WvWindowHandle @nook internal constructor(
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

	override fun invokeOnClose(handler: (WvWindowHandle) -> Unit): DisposableHandle

	override suspend fun awaitClose()

	override val onAwaitClose: SelectClause0

	// --

	companion object
}
