package kokoro.app

import okio.Source

expect object AppAssets

expect fun AppAssets.open(path: String): Source
