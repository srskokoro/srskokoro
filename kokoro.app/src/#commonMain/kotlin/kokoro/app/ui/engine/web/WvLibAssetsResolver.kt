package kokoro.app.ui.engine.web

import kokoro.app.LibAssets
import kokoro.app.open
import kokoro.app.openOrNull
import kokoro.internal.assert
import kokoro.internal.require
import okio.FileNotFoundException
import okio.Source
import okio.buffer
import okio.use
import kotlin.jvm.JvmField

/**
 * An implementation of [WebUriResolver] and [WebResource] that resolves against
 * bundled library assets – see [LibAssets].
 *
 * @param assetsDir an asset path to a directory containing the target assets.
 *   Must not start or end with slash.
 */
data class WvLibAssetsResolver(
	@JvmField val webOrigin: WebOrigin,
	@JvmField val assetsDir: String,
) : WebUriResolver, WebResource {

	constructor(
		webOrigin: String,
		assetsDir: String,
	) : this(
		webOrigin = WebOrigin.fromUri(webOrigin),
		assetsDir = assetsDir,
	)

	init {
		assert({
			!assetsDir.startsWith('/') && !assetsDir.endsWith('/')
		}, or = {
			"Argument `${::assetsDir.name}` must not start or end with slash."
		})
		require(assetsDir.startsWith("wv/"), or = {
			"""
			Only assets under "wv/" should be served.

			Access to assets outside of "wv/" is insecure as it may allow access to
			application files not meant to be exposed publicly, such as class files and
			compiled code.
			""".trimIndent()
		})
	}

	override fun resolve(uri: WebUri): WebResource? =
		if (webOrigin.matches(uri)) this else null

	override suspend fun apply(request: WebRequest): WebResponse {
		run<Unit> {
			val uri = request.url
			val path = uri.path() ?: return@run

			val a = StringBuilder(assetsDir)
			if (!path.startsWith('/')) a.append('/')
			a.append(path)

			val mimeType = if (path.endsWith('/')) {
				a.append("index.html")
				"text/html"
			} else {
				val q = path.lastIndexOf('/')
				// NOTE: Keeps the initial '/' at `q == 0`
				val ext = (if (q > 0) path.substring(q + 1) else path)
					.substringAfterLast('.', "")
				if (ext == HEAD_EXT) return@run
				MimeTypes.queryExt(ext)
			}

			val content = try {
				LibAssets.open(a.toString())
			} catch (ex: FileNotFoundException) {
				return@run
			}

			val headers = mutableMapOf<String, String>()
			LibAssets.openOrNull(a.append(DOT_HEAD_EXT).toString())?.let {
				loadResponseHeaders(headers, it)
			}

			return WebResponse(
				status = 200,

				mimeType = mimeType,
				charset = null, // Assume text assets have proper BOM

				headers = headers,

				contentLength = -1, content,
			)
		}
		return WebResponse(404)
	}

	companion object {

		@JvmField val HTTPX_WV_X = httpx(HOST_WV_X)

		@JvmField val HTTPX_UI_X = httpx(HOST_UI_X)

		@JvmField val HTTPX_RES_X = httpx(HOST_RES_X)

		@JvmField val HTTPX_RES_U = httpx(HOST_RES_U)

		@JvmField val PRESET = HTTPX_WV_X + HTTPX_UI_X + HTTPX_RES_X + HTTPX_RES_U

		@Suppress("NOTHING_TO_INLINE")
		inline fun httpx(host: String) = WvLibAssetsResolver("$HTTPX://$host", "wv/{$host}")

		// --

		private const val HEAD_EXT = "head"
		private const val DOT_HEAD_EXT = ".$HEAD_EXT"

		private fun loadResponseHeaders(out: MutableMap<String, String>, source: Source): Unit = source.buffer().use { buffered ->
			// Implementation reference:
			// - https://github.com/square/okhttp/blob/parent-4.12.0/okhttp/src/main/kotlin/okhttp3/internal/http1/HeadersReader.kt
			while (true) {
				val line = buffered.readUtf8Line() ?: break

				// TODO! Process `*.head` files at build time to automatically strip away blank lines and comment lines.
				if (line.isBlank()) continue // Special: allow blank lines (and ignore them).
				if (line.startsWith('#')) continue // Special: allow comment lines (and ignore them).

				val i = line.indexOf(':')

				val k: String
				val v: String

				// Implementation reference:
				// - https://github.com/square/okhttp/blob/parent-4.12.0/okhttp/src/main/kotlin/okhttp3/Headers.kt#L231
				if (i > 0) {
					// TODO! Process `*.head` files at build time to automatically make each header name lowercase.
					k = line.substring(0, i).lowercase()
					v = line.substring(i + 1)
				} else {
					k = ""
					v = if (i != 0) line else line.substring(1)
				}

				out[k] = v.trim()
			}
		}
	}
}
