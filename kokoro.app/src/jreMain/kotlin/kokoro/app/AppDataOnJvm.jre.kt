package kokoro.app

import kokoro.internal.OsDirs
import kokoro.internal.SPECIAL_USE_DEPRECATION
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

/** @see AppData.mainDir */
actual val AppDataOnJvm.mainDir: File inline get() = @Suppress("DEPRECATION_ERROR") `-AppDataOnJvm`.mainDir

/** @see AppData.cacheDir */
actual val AppDataOnJvm.cacheDir: File inline get() = @Suppress("DEPRECATION_ERROR") `-AppDataOnJvm`.cacheDir

/** @see AppData.logsDir */
@Suppress("UnusedReceiverParameter")
val AppDataOnJvm.logsDir: File inline get() = @Suppress("DEPRECATION_ERROR") `-AppDataOnJvm`.logsDir

// --

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal object `-AppDataOnJvm` {

	@JvmField val mainDir: File
	@JvmField val cacheDir: File
	@JvmField val logsDir: File
	@JvmField val logsDirOkio: Path

	init {
		val mainDir = run(fun(): File {
			var env = System.getenv("SRS_KOKORO_DATA")
			if (env.isNullOrEmpty()) env = AppBuildDesktop.APP_DATA_DIR_NAME
			var f = File(env)
			if (!f.isAbsolute) f = File(OsDirs.userData, env)
			f.mkdirs()
			return f
		})
		this.mainDir = mainDir
		this.cacheDir = run(fun(): File {
			var env = System.getenv("SRS_KOKORO_CACHE")
			if (env.isNullOrEmpty()) env = AppBuildDesktop.APP_DATA_DIR_NAME
			var f = File(env)
			if (!f.isAbsolute) f = File(OsDirs.userCache ?: OsDirs.userData, env)
			if (mainDir == f) f = File(f, "cache")
			f.mkdirs()
			return f
		})
		this.logsDir = run(fun(): File {
			var env = System.getenv("SRS_KOKORO_LOGS")
			if (env.isNullOrEmpty()) env = AppBuildDesktop.APP_DATA_DIR_NAME
			var f = File(env)
			if (!f.isAbsolute) f = File(OsDirs.userLogs ?: OsDirs.userData, env)
			if (mainDir == f) f = File(f, "logs")
			f.mkdirs()
			return f
		})
		this.logsDirOkio = logsDir.toOkioPath(false)
	}
}
