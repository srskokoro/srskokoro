package kokoro.app

import net.harawata.appdirs.AppDirsFactory
import java.io.File
import kotlin.io.path.createDirectories

@Suppress("NewApi")
actual object App {

	private const val APP_CACHE_DIR_ROOT_NAME = "SRSKokoroAppCache"
	private val platformDirs = AppDirsFactory.getInstance()

	private var _cacheMain: File? = null

	@JvmStatic
	actual val cacheMain: File
		get() = _cacheMain ?: File(
			platformDirs.getUserDataDir(APP_CACHE_DIR_ROOT_NAME, null, null, /* roaming = */ false),
			APP_CACHE_SCHEMA_VERSION_NAME,
		).also {
			it.toPath().createDirectories()
			_cacheMain = it
		}
}
