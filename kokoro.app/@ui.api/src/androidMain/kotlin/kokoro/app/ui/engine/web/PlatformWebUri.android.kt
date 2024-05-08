package kokoro.app.ui.engine.web

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias PlatformWebUri = android.net.Uri

@Suppress("ACTUAL_WITHOUT_EXPECT", "NOTHING_TO_INLINE")
actual inline fun PlatformWebUri(uri: String): PlatformWebUri = PlatformWebUri.parse(uri)
