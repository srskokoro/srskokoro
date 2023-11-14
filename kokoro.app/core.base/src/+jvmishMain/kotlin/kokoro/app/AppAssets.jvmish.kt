package kokoro.app

import java.io.InputStream

expect fun LibAssets.openStream(path: String): InputStream
