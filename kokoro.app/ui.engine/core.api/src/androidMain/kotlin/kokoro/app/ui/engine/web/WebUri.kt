package kokoro.app.ui.engine.web

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias WebUriValue = android.net.Uri


actual fun WebUri.scheme(): String? = value.scheme

actual fun WebUri.schemeSpecificPart(raw: Boolean): String =
	if (raw) value.encodedSchemeSpecificPart
	else value.schemeSpecificPart


actual fun WebUri.authority(raw: Boolean): String? =
	if (raw) value.encodedAuthority
	else value.authority

actual fun WebUri.userInfo(raw: Boolean): String? =
	if (raw) value.encodedUserInfo
	else value.userInfo

actual fun WebUri.host(): String? = value.host

actual fun WebUri.port(): Int = value.port


actual fun WebUri.path(raw: Boolean): String? =
	if (raw) value.encodedPath
	else value.path

actual fun WebUri.query(raw: Boolean): String? =
	if (raw) value.encodedQuery
	else value.query


actual fun WebUri.fragment(raw: Boolean): String? =
	if (raw) value.encodedFragment
	else value.fragment
