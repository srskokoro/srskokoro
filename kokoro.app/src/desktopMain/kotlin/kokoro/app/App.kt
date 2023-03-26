package kokoro.app

import net.harawata.appdirs.AppDirsFactory
import java.io.File
import kotlin.io.path.createDirectories

@Suppress("NewApi")
actual object App {

	private const val APP_CACHE_DIR_NAME = "SRSKokoroAppCache"
	private val platformDirs = AppDirsFactory.getInstance()

	private var _cacheData: File? = null

	@JvmStatic
	actual val cacheData: File
		get() = _cacheData ?: File(
			platformDirs.getUserDataDir(APP_CACHE_DIR_NAME, null, null, /* roaming = */ false),
			APP_CACHE_SCHEMA_VERSION_NAME
		).also {
			it.toPath().createDirectories()
			_cacheData = it
		}
}
