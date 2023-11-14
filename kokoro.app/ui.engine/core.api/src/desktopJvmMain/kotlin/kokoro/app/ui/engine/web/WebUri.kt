package kokoro.app.ui.engine.web

actual typealias WebUriValue = java.net.URI


actual fun WebUri.scheme(): String? = value.scheme

actual fun WebUri.schemeSpecificPart(raw: Boolean): String =
	if (raw) value.rawSchemeSpecificPart
	else value.schemeSpecificPart


actual fun WebUri.authority(raw: Boolean): String? =
	if (raw) value.rawAuthority
	else value.authority

actual fun WebUri.userInfo(raw: Boolean): String? =
	if (raw) value.rawUserInfo
	else value.userInfo

actual fun WebUri.host(): String? = value.host

actual fun WebUri.port(): Int = value.port


actual fun WebUri.path(raw: Boolean): String? =
	if (raw) value.rawPath
	else value.path

actual fun WebUri.query(raw: Boolean): String? =
	if (raw) value.rawQuery
	else value.query


actual fun WebUri.fragment(raw: Boolean): String? =
	if (raw) value.rawFragment
	else value.fragment
