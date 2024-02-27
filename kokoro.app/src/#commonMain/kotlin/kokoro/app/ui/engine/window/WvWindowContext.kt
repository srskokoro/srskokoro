package kokoro.app.ui.engine.window

import kotlin.jvm.JvmField

abstract class WvWindowContext(
	@JvmField val handle: WvWindowHandle,
) {
	abstract fun load(url: String)

	abstract fun finish()
}
