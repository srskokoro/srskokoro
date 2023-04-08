package kokoro.app

import okio.FileSystem
import okio.Path

internal object AppData_common {

	@JvmStatic
	fun ensureCacheMain(dirRoot: Path, fs: FileSystem): Path {
		val it = dirRoot / APP_DATA_SCHEMA_VERSION_NAME
		fs.createDirectories(it)
		if (fs.metadataOrNull(it)?.isDirectory != true) {
			// Let the following throw its own exception (so that we
			// don't have to throw our own customized one).
			fs.createDirectory(it, mustCreate = true)
		}
		return it
	}
}
