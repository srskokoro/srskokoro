package kokoro.app.ui.engine.web

import kokoro.app.LibAssets
import kokoro.app.open
import kokoro.internal.assert
import kokoro.internal.io.source
import okio.FileNotFoundException
import kotlin.jvm.JvmField

/**
 * An implementation of [WebUriResolver] and [WebResource] that resolves to
 * bundled library assets – see [LibAssets].
 *
 * @param assetsDir an asset path to a directory containing the target assets.
 *   Must not end with slash.
 */
data class WvLibAssetResolver(
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
		assert({ assetsDir.endsWith('/').not() }, or = {
			"Argument `${::assetsDir.name}` must not end with slash."
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

			return WebResponse(
				status = 200,

				mimeType = mimeType,
				charset = null, // Assume text assets have proper BOM

				headers = mutableMapOf(), // TODO Provide via special `*.head` assets

				contentLength = -1, content,
			)
		}
		return StatusResponse(404)
	}

	companion object {

		private const val HEAD_EXT = "head"

		private fun StatusResponse(status: Int): WebResponse {
			val bytes = status.toString().encodeToByteArray()

			return WebResponse(
				status = status,

				mimeType = "text/plain",
				// NOTE: Defaults to "US-ASCII" for the "text" MIME type
				// (according to MDN). See, https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#structure_of_a_mime_type
				charset = null,

				bytes.size.toLong(), bytes.source(),
			)
		}
	}
}
