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
	@JvmField val matchHost: String,
	@JvmField val assetsDir: String,
) : WebUriResolver, WebResource {

	init {
		assert({ assetsDir.endsWith('/').not() }, or = {
			"Argument `${::assetsDir.name}` must not end with slash."
		})
	}

	override fun resolve(uri: WebUri): WebResource? =
		if (uri.host() == matchHost) this else null

	override suspend fun apply(request: WebRequest): WebResponse {
		run<Unit> {
			val uri = request.url

			val path = uri.path() ?: return@run
			if (!path.startsWith('/')) return@run

			val ext = path
				.substring(path.lastIndexOf('/'))
				.substringAfterLast('.', "")

			if (ext == "head") return@run

			val content = try {
				LibAssets.open(assetsDir + path)
			} catch (ex: FileNotFoundException) {
				return@run
			}

			return WebResponse(
				status = 200,

				mimeType = MimeTypes.queryExt(ext),
				charset = null, // Assume text assets have proper BOM

				headers = mutableMapOf(), // TODO Provide via special `*.head` assets

				contentLength = -1, content,
			)
		}
		return StatusResponse(404)
	}

	companion object {

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
