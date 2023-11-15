package kokoro.app

import okio.Source

expect object LibAssets

expect fun LibAssets.open(path: String): Source
