package kokoro.app

/**
 * CONTRACT: The paths returned must all be absolute.
 */
expect object AppDataPlatformImpl {

	fun forRoamingRoot(): String

	fun forLocalRoot(): String

	fun forDeviceBoundRoot(): String

	fun forCacheRoot(): String
}
