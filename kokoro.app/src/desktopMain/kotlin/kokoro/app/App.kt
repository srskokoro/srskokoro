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

	private var _roamingDir: File? = null
	@JvmStatic actual val roamingDir: File
		get() = _roamingDir ?: run {
			File(platformDirs.getUserDataDir(appDataDirName, null, null, /* roaming = */ true)).takeUnless { it.canonicalPath == localDir.canonicalPath }
			?: File(platformDirs.getUserDataDir("$appDataDirName-roaming", null, null, /* roaming = */ false))
		}.also { it.toPath().createDirectories() }
			.also { _roamingDir = it }
}
