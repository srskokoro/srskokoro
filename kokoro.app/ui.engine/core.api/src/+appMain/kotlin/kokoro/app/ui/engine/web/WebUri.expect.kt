package kokoro.app.ui.engine.web

import kotlin.jvm.JvmInline

@JvmInline
value class WebUri(val value: WebUriValue) {
	constructor(uri: String) : this(getWebUriValue(uri))
}

expect class WebUriValue

internal expect fun getWebUriValue(uri: String): WebUriValue

expect fun WebUri.scheme(): String?
expect fun WebUri.schemeSpecificPart(raw: Boolean = false): String

expect fun WebUri.authority(raw: Boolean = false): String?
expect fun WebUri.userInfo(raw: Boolean = false): String?
expect fun WebUri.host(): String?
expect fun WebUri.port(): Int

expect fun WebUri.path(raw: Boolean = false): String?
expect fun WebUri.query(raw: Boolean = false): String?

expect fun WebUri.fragment(raw: Boolean = false): String?
