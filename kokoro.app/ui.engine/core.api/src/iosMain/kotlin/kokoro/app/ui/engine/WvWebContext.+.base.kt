package kokoro.app.ui.engine

internal actual fun WvWebContext_platformInit() = Unit

actual fun WvWebContext.Companion.appendOrigin(out: StringBuilder, webContextDomain: String): StringBuilder {
	return out.append("wv://$webContextDomain/")
}
