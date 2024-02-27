package kokoro.app.ui.engine.window

abstract class WvWindowContext {

	abstract val handle: WvWindowHandle

	abstract fun load(url: String)

	abstract fun finish()
}
