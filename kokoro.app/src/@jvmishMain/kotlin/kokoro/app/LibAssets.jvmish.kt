package kokoro.app

import kokoro.internal.io.asSource
import okio.FileNotFoundException
import okio.Source
import java.io.InputStream

actual fun LibAssets.openOrNull(path: String): Source? = openStreamOrNull(path)?.asSource()

@Throws(FileNotFoundException::class)
actual fun LibAssets.open(path: String): Source = openStream(path).asSource()

// --

expect fun LibAssets.openStreamOrNull(path: String): InputStream?

@Throws(FileNotFoundException::class)
expect fun LibAssets.openStream(path: String): InputStream
