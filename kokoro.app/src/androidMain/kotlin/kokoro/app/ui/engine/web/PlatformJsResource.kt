package kokoro.app.ui.engine.web

import okio.Buffer

class PlatformJsResource : BasePlatformJsResource() {
	override fun feed(out: Buffer, request: WebRequest) {
		super.feed(out, request)
		// TODO Saved states handling
	}
}
