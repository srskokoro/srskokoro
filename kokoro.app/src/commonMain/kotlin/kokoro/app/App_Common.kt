package kokoro.app

import okio.FileSystem
import okio.IOException
import okio.Path
import kotlin.jvm.JvmStatic

internal object App_Common {

	@JvmStatic
	fun ensureCacheMain(dirRoot: Path, fs: FileSystem): Path {
		return (dirRoot / APP_CACHE_SCHEMA_VERSION_NAME).also {
			fs.apply {
				createDirectories(it)
				if (metadataOrNull(it)?.isDirectory != true) {
					throw IOException("failed to create directory: $it")
				}
			}
		}
	}
}
