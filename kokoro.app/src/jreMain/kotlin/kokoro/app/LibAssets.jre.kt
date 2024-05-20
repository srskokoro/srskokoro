package kokoro.app

import okio.FileNotFoundException
import java.io.InputStream

actual object LibAssets {
	@JvmField val loader = LibAssets::class.java.classLoader!!
}

@Throws(FileNotFoundException::class)
actual fun LibAssets.openStream(path: String): InputStream =
	loader.getResourceAsStream(path) ?: throw E_MissingAsset(path)

private fun E_MissingAsset(path: String) = FileNotFoundException("Asset file: $path")
