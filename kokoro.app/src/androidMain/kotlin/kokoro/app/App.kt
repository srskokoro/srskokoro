package kokoro.app

import android.app.Application
import java.io.File

actual object App {

	@JvmField val context: Application =
		MainApplication.`$$inst` ?: throw UninitializedPropertyAccessException("Main application not yet initialized")

	// --

	private var _localData: File? = null

	@JvmStatic
	actual val localData: File
		get() = _localData ?: File(
			context.noBackupFilesDir, // Assumed thread-safe
			APP_DATA_SCHEMA_VERSION_DIR_NAME
		).also {
			it.mkdir() // TODO Fail fast!
			_localData = it
		}
}
