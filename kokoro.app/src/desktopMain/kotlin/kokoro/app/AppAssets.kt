package kokoro.app

import okio.FileNotFoundException
import okio.Source
import okio.source
import java.io.InputStream

actual object AppAssets {
	@JvmField val resourceLoader: ClassLoader = AppAssets::class.java.classLoader
}

actual fun AppAssets.open(path: String): Source =
	(resourceLoader.getResourceAsStream(path) ?: throw E_MissingAsset(path))
		.source()

actual fun AppAssets.openStream(path: String): InputStream =
	resourceLoader.getResourceAsStream(path) ?: throw E_MissingAsset(path)

private fun E_MissingAsset(path: String) = FileNotFoundException("Asset file: $path")
