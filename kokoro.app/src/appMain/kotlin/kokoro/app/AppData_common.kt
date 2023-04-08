package kokoro.app

import kokoro.internal.io.SYSTEM
import okio.FileSystem
import okio.Path
import kotlin.jvm.JvmStatic

internal object AppData_common {

	@JvmStatic
	fun ensureDirMain(dirRoot: Path, fs: FileSystem = FileSystem.SYSTEM): Path {
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
