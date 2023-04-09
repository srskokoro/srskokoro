package kokoro.app

import kokoro.app.AppDataDirCode.*
import kokoro.app.AppDataPlatformDefaults.forCacheRoot
import kokoro.app.AppDataPlatformDefaults.forDefaultRoot
import kokoro.app.AppDataPlatformDefaults.forLocalRoot
import kokoro.app.AppDataPlatformDefaults.forRoamingRoot
import kokoro.internal.io.SYSTEM
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

object AppData {
	const val SCHEMA_VERSION = 0
	private val SCHEMA_VERSION_NAME = getSchemaVersionName(SCHEMA_VERSION)

	@JvmStatic fun getDirMainName(code: AppDataDirCode, version: Int = SCHEMA_VERSION): String = run {
		if (version == SCHEMA_VERSION) "$SCHEMA_VERSION_NAME${code.value}"
		else "${getSchemaVersionName(version)}${code.value}"
	}

	/**
	 * The primary directory for storing roaming app data. All roaming app data
	 * should usually go under this directory. This directory is always a
	 * subdirectory of the roaming app data directory root.
	 *
	 * The roaming app data are those that may participate in cloud sync.
	 */
	@JvmStatic val roamingMain get() = RoamingMain.value

	private object RoamingMain {
		@JvmField val value: Path = (run<String> {
			forRoamingRoot() + Path.DIRECTORY_SEPARATOR
		} + getDirMainName(R)).toPath(true).ensureDirs()
	}

	/**
	 * The primary directory for storing local app data. All local app data
	 * should usually go under this directory. This directory is always a
	 * subdirectory of the local app data directory root.
	 *
	 * The local app data are those that does not participate in cloud sync, but
	 * may participate in device-to-device migrations.
	 */
	@JvmStatic val localMain get() = LocalMain.value

	private object LocalMain {
		@JvmField val value: Path = (run<String> {
			forLocalRoot() + Path.DIRECTORY_SEPARATOR
		} + getDirMainName(L)).toPath(true).ensureDirs()
	}

	/**
	 * The primary directory for storing device-bound local app data. All
	 * device-bound local app data should usually go under this directory. This
	 * directory is always a subdirectory of the device-bound local app data
	 * directory root.
	 *
	 * The device-bound local app data are those that should never be
	 * transferred to other devices, such as, device identifiers meant to
	 * uniquely identify the device – see also, “[Best practices for unique identifiers | Android Developers](https://developer.android.com/training/articles/user-data-ids)”
	 */
	@JvmStatic val deviceBoundMain get() = DeviceBoundMain.value

	private object DeviceBoundMain {
		@JvmField val value: Path = (run<String> {
			forDefaultRoot() + Path.DIRECTORY_SEPARATOR
		} + getDirMainName(D)).toPath(true).ensureDirs()
	}

	/**
	 * The primary directory for storing the app's cache data. All cache data
	 * should usually go under this directory. This directory is always a
	 * subdirectory of the app's cache data directory root.
	 *
	 * Cache data may be deleted automatically by the OS whenever it needs to
	 * free up storage space.
	 */
	@JvmStatic val cacheMain get() = CacheMain.value

	private object CacheMain {
		@JvmField val value: Path = (run<String> {
			forCacheRoot() + Path.DIRECTORY_SEPARATOR
		} + getDirMainName(C)).toPath(true).ensureDirs()
	}
}

private fun getSchemaVersionName(version: Int) = version.toString().padStart(3, '0')

private fun Path.ensureDirs(): Path {
	val fs = FileSystem.SYSTEM
	fs.createDirectories(this)
	if (fs.metadataOrNull(this)?.isDirectory != true) {
		// Let the following throw its own exception (so that we don't have to
		// throw our own customized one).
		fs.createDirectory(this, mustCreate = true)
	}
	return this
}
