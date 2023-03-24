package kokoro.app

import android.app.Application
import java.io.File

actual object App {

	@JvmField val context: Application =
		MainApplication.`$$inst` ?: throw UninitializedPropertyAccessException("Main application not yet initialized")

	// --

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
			context.noBackupFilesDir, // Assumed thread-safe
			APP_DATA_SCHEMA_VERSION_DIR_NAME
		).also {
			it.mkdir()
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			_localRoot = it.parentFile
			_localData = it
		}
}
