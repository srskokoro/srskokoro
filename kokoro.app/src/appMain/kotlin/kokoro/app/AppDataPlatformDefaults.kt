package kokoro.app

expect object AppDataPlatformDefaults {
	fun forDefaultRoot(): String

	fun forRoamingRoot(): String
	fun forLocalRoot(): String
	fun forCacheRoot(): String
}
