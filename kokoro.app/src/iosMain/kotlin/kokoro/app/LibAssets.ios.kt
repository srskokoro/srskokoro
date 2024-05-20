package kokoro.app

import okio.FileNotFoundException
import okio.Source

actual object LibAssets

actual fun LibAssets.openOrNull(path: String): Source? {
	TODO("Not yet implemented")
}

@Throws(FileNotFoundException::class)
actual fun LibAssets.open(path: String): Source {
	TODO("Not yet implemented")
}
