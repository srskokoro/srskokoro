package kokoro.app.ui.engine.web

import kokoro.internal.assert
import java.net.URLDecoder

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

actual fun WebUri.port(raw: Boolean): Int {
	var port = value.port
	if (raw) return port

	if (port < 0) {
		val scheme = value.scheme
		if (scheme != null) {
			port = WebUri.getPortForScheme(scheme)
		} else assert({ port == -1 }) {
			"Per docs, `${PlatformWebUri::getPort.name}()` can never return a negative integer other than -1"
		}
	}
	return port
}


actual fun WebUri.path(raw: Boolean): String? =
	if (raw) value.rawPath
	else value.path

actual fun WebUri.lastPathSegment(): String? {
	val rawPath = value.rawPath
	if (rawPath != null) {
		return URLDecoder.decode(rawPath.substringAfterLast('/'), Charsets.UTF_8)
	}
	return null
}


actual fun WebUri.query(raw: Boolean): String? =
	if (raw) value.rawQuery
	else value.query

actual fun WebUri.fragment(raw: Boolean): String? =
	if (raw) value.rawFragment
	else value.fragment
