package kokoro.app

expect object AppDataPlatformImpl {

	fun forRoamingRoot(): String

	fun forLocalRoot(): String

	fun forDeviceBoundRoot(): String

	fun forCacheRoot(): String
}
