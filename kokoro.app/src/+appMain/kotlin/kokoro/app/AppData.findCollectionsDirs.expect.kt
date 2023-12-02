package kokoro.app

import okio.Path

expect fun AppData.findCollectionsDirs(): List<Path>
