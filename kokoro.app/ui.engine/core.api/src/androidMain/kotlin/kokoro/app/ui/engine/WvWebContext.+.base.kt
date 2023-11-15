package kokoro.app.ui.engine

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun WvWebContext_platformInit() = Unit

actual fun WvWebContext.Companion.appendOrigin(out: StringBuilder, webContextDomain: String): StringBuilder {
	return out.append("https://$webContextDomain.w/")
}
