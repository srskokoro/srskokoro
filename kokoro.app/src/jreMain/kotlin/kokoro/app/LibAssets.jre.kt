package kokoro.app

import okio.FileNotFoundException
import java.io.File
import java.io.IOException
import java.io.InputStream

actual object LibAssets {
	@JvmField val loader = LibAssets::class.java.classLoader!!
}

actual fun LibAssets.openStreamOrNull(path: String): InputStream? {
	val url = loader.getResource(path)
	if (url != null) try {
		if (url.protocol != "file" || !File(url.path).isDirectory) {
			return url.openStream()
		} else {
			// NOTE: Necessary as the default implementation is insecure -- it
			// would return a list of directory entries instead.
			// - See also, https://stackoverflow.com/a/20107785
			return InputStream.nullInputStream()
		}
	} catch (_: IOException) {
		// Ignore.
	}
	return null
}

@Throws(FileNotFoundException::class)
actual fun LibAssets.openStream(path: String): InputStream =
	openStreamOrNull(path) ?: throw E_MissingAsset(path)

private fun E_MissingAsset(path: String) = FileNotFoundException("Asset file: $path")
