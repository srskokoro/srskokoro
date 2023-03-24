package kokoro.app

import net.harawata.appdirs.AppDirsFactory
import java.io.File
import kotlin.io.path.createDirectories

@Suppress("NewApi")
actual object App {

	private const val appDataDirName = "srskokoro"
	private val platformDirs = AppDirsFactory.getInstance()

	private var _localDir: File? = null
	@JvmStatic actual val localDir: File
		get() = _localDir ?: File(platformDirs.getUserDataDir(appDataDirName, null, null, /* roaming = */ false))
			.also { it.toPath().createDirectories() }
			.also { _localDir = it }
}
