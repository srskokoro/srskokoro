package kokoro.app.ui.engine

internal actual fun WvWebContext_platformInit() = Unit

actual fun WvWebContext.Companion.appendOrigin(out: StringBuilder, webContextDomain: String): StringBuilder {
	out.append("wv://")
	out.append(webContextDomain)
	return out
}
