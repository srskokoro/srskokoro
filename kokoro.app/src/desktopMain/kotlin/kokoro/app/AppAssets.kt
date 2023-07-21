package kokoro.app

import okio.FileNotFoundException
import okio.Source
import okio.source
import java.io.InputStream

actual object AppAssets

private val cl = AppAssets::class.java.classLoader

actual fun AppAssets.open(path: String): Source =
	(cl.getResourceAsStream(path) ?: throw errorForMissingAsset(path))
		.source()

actual fun AppAssets.openStream(path: String): InputStream =
	cl.getResourceAsStream(path) ?: throw errorForMissingAsset(path)

private fun errorForMissingAsset(path: String) = FileNotFoundException("Asset file: $path")
