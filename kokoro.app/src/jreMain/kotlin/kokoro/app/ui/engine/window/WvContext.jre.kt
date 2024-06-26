package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.web.PlatformJsResource
import kokoro.app.ui.engine.web.WebResource
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain

@nook internal class WvContextImpl(
	handle: WvWindowHandle,
	private val frame: WvWindowFrame,
) : WvContext(handle, frame.scope) {

	override var title: CharSequence?
		@MainThread get() = frame.title
		@MainThread set(v) {
			assertThreadMain()
			frame.title = if (v is String) v else v?.toString()
		}

	@MainThread
	override fun load(url: String) {
		frame.loadUrl(url) // Asserts thread main
	}

	@MainThread
	override fun finish() {
		assertThreadMain()
		frame.dispose()
	}

	@MainThread
	override fun <T> loadOldState(bus: UiBus<T>): T? {
		// We never have "old states" when on desktop JVM (JRE).
		return null
	}

	override fun initPlatformJs(): WebResource = PlatformJsResource
}
