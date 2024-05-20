package kokoro.app.ui.engine.web

import kotlin.jvm.JvmInline

@JvmInline
value class WebUri(val value: PlatformWebUri) {

	constructor(uri: String) : this(PlatformWebUri(uri))

	override fun toString() = value.toString()

	companion object {
		fun getPortForScheme(scheme: String) = when (scheme) {
			"http" -> 80
			"https" -> 443
			else -> -1
		}
	}
}

expect fun WebUri.scheme(): String?

/** @see ssp */
expect fun WebUri.schemeSpecificPart(raw: Boolean = false): String

/** Shorthand for [schemeSpecificPart]`()` */
@Suppress("NOTHING_TO_INLINE")
inline fun WebUri.ssp(raw: Boolean = false): String = schemeSpecificPart(raw)

expect fun WebUri.authority(raw: Boolean = false): String?
expect fun WebUri.userInfo(raw: Boolean = false): String?
expect fun WebUri.host(): String?
expect fun WebUri.port(raw: Boolean = false): Int

expect fun WebUri.path(raw: Boolean = false): String?
expect fun WebUri.lastPathSegment(): String?

expect fun WebUri.query(raw: Boolean = false): String?
expect fun WebUri.fragment(raw: Boolean = false): String?
