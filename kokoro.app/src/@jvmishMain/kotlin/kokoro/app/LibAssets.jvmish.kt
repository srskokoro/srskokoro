package kokoro.app

import kokoro.internal.io.asSource
import okio.Source
import java.io.InputStream

actual fun LibAssets.open(path: String): Source = openStream(path).asSource()

expect fun LibAssets.openStream(path: String): InputStream
