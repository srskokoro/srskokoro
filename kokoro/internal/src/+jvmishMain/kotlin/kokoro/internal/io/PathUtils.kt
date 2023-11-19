package kokoro.internal.io

import okio.Path

actual inline val Path.exists get() = toFile().exists()

actual inline val Path.isRegularFile get() = toFile().isFile

actual inline val Path.isDirectory get() = toFile().isDirectory

actual fun Path.ensureDirs(): Path {
	val f = toFile()
	if (f.isDirectory || f.mkdirs()) return this
	@Suppress("DEPRECATION")
	return `-ensureDirs-fallback`(this)
}
