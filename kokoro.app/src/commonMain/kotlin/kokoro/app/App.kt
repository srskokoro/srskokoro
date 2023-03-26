package kokoro.app

import java.io.File

const val APP_DATA_SCHEMA_VERSION = 0

val APP_DATA_SCHEMA_VERSION_NAME = APP_DATA_SCHEMA_VERSION.toString().padStart(4, '0')

expect object App {

	@JvmStatic val localData: File
}
