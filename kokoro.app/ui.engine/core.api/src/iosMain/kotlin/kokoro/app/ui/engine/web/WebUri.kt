package kokoro.app.ui.engine.web

actual class WebUriValue

internal actual fun getWebUriValue(uri: String): WebUriValue = TODO("Not yet implemented")

actual fun WebUri.scheme(): String? = TODO("Not yet implemented")
actual fun WebUri.schemeSpecificPart(raw: Boolean): String = TODO("Not yet implemented")

actual fun WebUri.authority(raw: Boolean): String? = TODO("Not yet implemented")
actual fun WebUri.userInfo(raw: Boolean): String? = TODO("Not yet implemented")
actual fun WebUri.host(): String? = TODO("Not yet implemented")
actual fun WebUri.port(raw: Boolean): Int = TODO("Not yet implemented")

actual fun WebUri.path(raw: Boolean): String? = TODO("Not yet implemented")
actual fun WebUri.query(raw: Boolean): String? = TODO("Not yet implemented")

actual fun WebUri.fragment(raw: Boolean): String? = TODO("Not yet implemented")
