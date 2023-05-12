package kokoro.internal.io

import okio.FileSystem
import okio.IOException
import okio.Path

internal fun `-ensureDirs-fallback`(dir: Path): Path {
	val fs = FileSystem.SYSTEM
	try {
		// Let the following throw its own exception (so that we don't have to
		// throw our own customized one).
		fs.createDirectory(dir, mustCreate = true)
	} catch (ex: IOException) {
		// Doesn't throw if it ended up existing anyway
		if (fs.metadataOrNull(dir)?.isDirectory != true) {
			throw ex
		}
	}
	return dir
}
