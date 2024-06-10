package kokoro.app.ui.engine.web

import okio.Buffer
import java.util.UUID

class PlatformJsResource : BasePlatformJsResource() {

	override fun feed(out: Buffer, request: WebRequest) {
		super.feed(out, request)
		// TODO Saved states handling
	}

	companion object {

		val SECRET = buildString(26) {
			val uuid = UUID.randomUUID()

			var s = java.lang.Long.toUnsignedString(uuid.mostSignificantBits, 32)
			var n = 13 - s.length
			while (--n >= 0) append('0')
			append(s)

			s = java.lang.Long.toUnsignedString(uuid.leastSignificantBits, 32)
			n = 13 - s.length
			while (--n >= 0) append('0')
			append(s)
		}
	}
}
