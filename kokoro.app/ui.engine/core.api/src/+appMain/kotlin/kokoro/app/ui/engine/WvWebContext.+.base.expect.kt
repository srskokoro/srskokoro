package kokoro.app.ui.engine

internal expect fun WvWebContext_platformInit()

fun WvWebContext.Companion.getOrigin(webContextDomain: String): String =
	buildString { appendOrigin(this, webContextDomain) }

expect fun WvWebContext.Companion.appendOrigin(out: StringBuilder, webContextDomain: String): StringBuilder
