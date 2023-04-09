package kokoro.app

expect object AppDataPlatformImpl {
	fun forDefaultRoot(): String

	fun forRoamingRoot(): String
	fun forLocalRoot(): String
	fun forCacheRoot(): String
}
