package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/** @see AppData.mainDir */
actual val AppDataOnJvm.mainDir: File inline get() = @Suppress("DEPRECATION_ERROR") `-AppDataOnJvm`.mainDir

/** @see AppData.cacheDir */
actual val AppDataOnJvm.cacheDir: File inline get() = @Suppress("DEPRECATION_ERROR") `-AppDataOnJvm`.cacheDir

// --

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal object `-AppDataOnJvm` {

	@JvmField val mainDir: File
	@JvmField val cacheDir: File

	init {
		val app = CoreApplication.get()
		mainDir = File(app.filesDir, "m").apply { mkdir() }

		val internalCacheDir = app.cacheDir
		val externalCacheDir = app.externalCacheDir
		if (externalCacheDir != null) {
			@OptIn(DelicateCoroutinesApi::class)
			GlobalScope.launch(Dispatchers.IO) {
				internalCacheDir.deleteRecursively()
			}
			cacheDir = externalCacheDir
		} else {
			cacheDir = internalCacheDir
		}
	}
}
