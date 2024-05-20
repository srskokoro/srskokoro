package kokoro.app

import kokoro.internal.io.asSource
import okio.FileNotFoundException
import okio.Source
import java.io.InputStream

@Throws(FileNotFoundException::class)
actual fun LibAssets.open(path: String): Source = openStream(path).asSource()

@Throws(FileNotFoundException::class)
expect fun LibAssets.openStream(path: String): InputStream
