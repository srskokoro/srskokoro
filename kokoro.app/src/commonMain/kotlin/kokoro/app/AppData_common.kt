package kokoro.app

import okio.FileSystem
import okio.Path

internal object AppData_common {

	@JvmStatic
	fun ensureCacheMain(dirRoot: Path, fs: FileSystem): Path {
		return (dirRoot / APP_DATA_SCHEMA_VERSION_NAME).also {
			fs.apply {
				createDirectories(it)
				if (metadataOrNull(it)?.isDirectory != true) {
					// Let the following throw its own exception (so that we
					// don't have to throw our own customized one).
					createDirectory(it, mustCreate = true)
				}
			}
		}
	}
}
