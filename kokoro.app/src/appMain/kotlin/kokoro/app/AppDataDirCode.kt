package kokoro.app

enum class AppDataDirCode {

	/** A single character suffix used by the roaming app data directory. */
	R,

	/** A single character suffix used by the local app data directory. */
	L,

	/** A single character suffix used by the device-bound app data directory. */
	D,

	/** A single character suffix used by the app's cache data directory. */
	C,

	; // --

	val value: Char = name.single()
}
