package kokoro.app

import net.harawata.appdirs.AppDirsFactory
import java.io.File
import kotlin.io.path.createDirectories

@Suppress("NewApi")
actual object App {

	private const val APP_DATA_DIR_NAME = "SRSKokoroApp"
	private val platformDirs = AppDirsFactory.getInstance()

	private lateinit var _localRoot: File
	private var _localData: File? = null

	@JvmStatic
	actual val localRoot: File
		get() {
			localData // Force init (see below)
			return _localRoot
		}

	@JvmStatic
	actual val localData: File
		get() = _localData ?: File(
			platformDirs.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ false),
			APP_DATA_SCHEMA_VERSION_DIR_NAME
		).also {
			it.toPath().createDirectories()
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			_localRoot = it.parentFile
			_localData = it
		}
}
