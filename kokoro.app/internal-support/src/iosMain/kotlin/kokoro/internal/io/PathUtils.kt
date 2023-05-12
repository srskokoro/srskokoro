package kokoro.internal.io

import okio.FileSystem
import okio.Path

actual inline val Path.exists get() = FileSystem.SYSTEM.exists(this)

actual inline val Path.isRegularFile get() = FileSystem.SYSTEM.metadataOrNull(this)?.isRegularFile == true

actual inline val Path.isDirectory get() = FileSystem.SYSTEM.metadataOrNull(this)?.isDirectory == true

actual fun Path.ensureDirs(): Path {
	val fs = FileSystem.SYSTEM
	fs.createDirectories(this, mustCreate = false)
	if (fs.metadataOrNull(this)?.isDirectory == true) return this
	return `-ensureDirs-fallback`(this)
}
