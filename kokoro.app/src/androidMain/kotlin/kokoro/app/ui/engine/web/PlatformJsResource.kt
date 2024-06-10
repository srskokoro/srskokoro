package kokoro.app.ui.engine.web

import kokoro.app.ui.engine.UiStatesSaver
import kokoro.internal.assert
import okio.Buffer
import java.util.UUID

class PlatformJsResource : BasePlatformJsResource() {

	override fun feed(out: Buffer, request: WebRequest) {
		super.feed(out, request)
		with(UiStatesSaver.JS_DEF) {
			out.writeUtf8(START)
			out.writeUtf8(PLATFORM_JS_SECRET)
			out.writeUtf8(END)
		}
	}
}

const val PLATFORM_JS_SECRET_n = 26

val PLATFORM_JS_SECRET = buildString(PLATFORM_JS_SECRET_n) {
	val uuid = UUID.randomUUID()

	var s = java.lang.Long.toUnsignedString(uuid.mostSignificantBits, 32)
	var n = 13 - s.length
	while (--n >= 0) append('0')
	append(s)

	s = java.lang.Long.toUnsignedString(uuid.leastSignificantBits, 32)
	n = 13 - s.length
	while (--n >= 0) append('0')
	append(s)

	assert({ length == PLATFORM_JS_SECRET_n })
}
