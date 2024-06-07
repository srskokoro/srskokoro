package kokoro.app.ui.engine.web

import okio.Buffer

object PlatformJsResource : BasePlatformJsResource() {
	override fun feed(out: Buffer, request: WebRequest) {
		super.feed(out, request)
		out.writeUtf8("$UI_STATES_LOADER=()=>($UI_STATES_LOADER=null,{})")
	}
}
