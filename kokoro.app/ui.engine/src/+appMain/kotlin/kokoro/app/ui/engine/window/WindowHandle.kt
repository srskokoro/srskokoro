package kokoro.app.ui.engine.window

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface WindowHandle<A : WindowArgs> {

	val windowId: WindowId<A>

	fun load(newArgs: A)

	fun alive(): StateFlow<Boolean>

	fun <T> status(statusBus: WindowCore.StatusBus<T>): SharedFlow<T>
}
