package kokoro.app

import java.io.File

expect object App {

	@JvmStatic val localDir: File
}
