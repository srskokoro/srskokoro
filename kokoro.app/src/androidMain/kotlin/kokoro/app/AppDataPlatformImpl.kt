package kokoro.app

import java.io.File

actual object AppDataPlatformImpl {

	@JvmStatic actual fun forRoamingRoot(): String =
		MainApplication.get().filesDir // Assumed thread-safe
			.let { File(it, "roaming") }.path

	@JvmStatic actual fun forLocalRoot(): String =
		MainApplication.get().filesDir // Assumed thread-safe
			.let { File(it, "local") }.path

	@Suppress("NOTHING_TO_INLINE")
	@JvmStatic actual inline fun forDeviceBoundRoot(): String =
		MainApplication.get().noBackupFilesDir // Assumed thread-safe
			.path

	@JvmStatic actual fun forCacheRoot(): String =
		MainApplication.get().cacheDir // Assumed thread-safe
			.path
}
