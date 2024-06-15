package kokoro.app.ui.engine.web

import kokoro.app.ui.engine.UI_STATES_LOADER
import okio.Buffer

object PlatformJsResource : BasePlatformJsResource() {
	override fun feed(out: Buffer) {
		super.feed(out)
		out.writeUtf8("\n$UI_STATES_LOADER=()=>($UI_STATES_LOADER=null,\"{}\")")
	}
}
