package kokoro.app

import java.io.InputStream

expect fun AppAssets.openStream(path: String): InputStream
