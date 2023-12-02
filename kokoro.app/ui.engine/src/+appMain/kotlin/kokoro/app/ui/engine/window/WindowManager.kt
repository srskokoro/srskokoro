package kokoro.app.ui.engine.window

interface WindowManager {

	fun <A : WindowArgs> launch(windowId: WindowId<A>, args: A): WindowHandle<A>

	fun <A : WindowArgs> get(windowId: WindowId<A>): WindowHandle<A>
}
