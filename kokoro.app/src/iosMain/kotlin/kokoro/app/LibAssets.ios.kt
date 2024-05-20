package kokoro.app

import okio.FileNotFoundException
import okio.Source

actual object LibAssets

@Throws(FileNotFoundException::class)
actual fun LibAssets.open(path: String): Source {
	TODO("Not yet implemented")
}
