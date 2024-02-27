package kokoro.app.ui.engine.window

internal class WvContextImpl(
	@JvmField val handle_: WvWindowHandleImpl,
) : WvContext() {

	override val handle: WvWindowHandle
		get() = handle_

	override fun load(url: String) {
		TODO("Not yet implemented")
	}

	override fun finish() {
		handle_.activity?.finish()
	}
}
