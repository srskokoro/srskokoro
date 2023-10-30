package kokoro.app

import okio.Path

object AppData {

	/**
	 * The primary directory for storing local app data.
	 *
	 * A file under this directory may be device-bound, that is, it might be
	 * expected to never be transferred to other devices, e.g., device
	 * identifiers meant to uniquely identify the device – see also,
	 * “[Best practices for unique identifiers | Android Developers](https://developer.android.com/training/articles/user-data-ids)”
	 */
	inline val mainDir: Path get() = @Suppress("DEPRECATION") AppDataImpl.mainDir

	inline val collectionsDir: Path? get() = @Suppress("DEPRECATION") AppDataImpl.collectionsDir

	// --

	val mainLogsDir: Path = mainDir / "logs"
}
