package kokoro.internal

import com.sun.jna.platform.win32.Guid.GUID
import com.sun.jna.platform.win32.KnownFolders
import com.sun.jna.platform.win32.Shell32Util
import com.sun.jna.platform.win32.Win32Exception
import java.io.File

object OsDirs {

	val userHome: File inline get() = @Suppress("DEPRECATION_ERROR") `OsDirsImpl-home`.VALUE

	val userData: File inline get() = @Suppress("DEPRECATION_ERROR") `-userData`.VALUE

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object `-userData` {
		@JvmField val VALUE = OsDirsImpl.INSTANCE.userData()
	}

	val userCache: File? inline get() = @Suppress("DEPRECATION_ERROR") `-userCache`.VALUE

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object `-userCache` {
		@JvmField val VALUE = OsDirsImpl.INSTANCE.userCache()
	}

	val userLogs: File? inline get() = @Suppress("DEPRECATION_ERROR") `-userLogs`.VALUE

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object `-userLogs` {
		@JvmField val VALUE = OsDirsImpl.INSTANCE.userLogs()
	}

	// --

	val userDocuments: File inline get() = @Suppress("DEPRECATION_ERROR") `-userDocuments`.VALUE

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object `-userDocuments` {
		@JvmField val VALUE = OsDirsImpl.INSTANCE.userDocuments()
	}

	val userDownloads: File inline get() = @Suppress("DEPRECATION_ERROR") `-userDownloads`.VALUE

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object `-userDownloads` {
		@JvmField val VALUE = OsDirsImpl.INSTANCE.userDownloads()
	}
}

// --

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal object `OsDirsImpl-home` {
	@JvmField val VALUE: File = File(System.getProperty("user.home"))
		.run { if (isAbsolute) this else absoluteFile }
}

private abstract class OsDirsImpl {

	protected val home inline get() = @Suppress("DEPRECATION_ERROR") `OsDirsImpl-home`.VALUE

	abstract fun userData(): File

	abstract fun userCache(): File?

	abstract fun userLogs(): File?

	// --

	abstract fun userDocuments(): File

	abstract fun userDownloads(): File

	// --

	companion object {
		@JvmField val INSTANCE = when (Os.current) {
			Os.WINDOWS -> OsDirsImplForWindows()
			Os.MACOS -> OsDirsImplForMacOs()
			Os.LINUX -> OsDirsImplForLinux()
		}
	}
}

private class OsDirsImplForWindows : OsDirsImpl() {

	override fun userData() = getKnownFolder(KnownFolders.FOLDERID_LocalAppData)

	override fun userCache(): File? = null

	override fun userLogs(): File? = null

	// --

	override fun userDocuments() = getKnownFolder(KnownFolders.FOLDERID_Documents)

	override fun userDownloads() = getKnownFolder(KnownFolders.FOLDERID_Downloads)

	// --

	private fun getKnownFolder(folderId: GUID): File {
		// See, https://github.com/harawata/appdirs/blob/appdirs-1.2.2/src/main/java/net/harawata/appdirs/impl/ShellFolderResolver.java#L28
		return File(try {
			Shell32Util.getKnownFolderPath(folderId)
		} catch (ex: Win32Exception) {
			error("SHGetKnownFolderPath returns an error: ${ex.errorCode}")
		} catch (ex: UnsatisfiedLinkError) {
			throw Os.E_Unsupported()
		}).absoluteFile
	}
}

private class OsDirsImplForMacOs : OsDirsImpl() {

	override fun userData() = File(home, "Library/Application Support")

	override fun userCache() = File(home, "Library/Caches")

	override fun userLogs() = File(home, "Library/Logs")

	// --

	override fun userDocuments() = File(home, "Documents")

	override fun userDownloads() = File(home, "Downloads")
}

private class OsDirsImplForLinux : OsDirsImpl() {

	override fun userData() = fromEnv("XDG_DATA_HOME") ?: File(home, ".local/share")

	override fun userCache() = fromEnv("XDG_CACHE_HOME") ?: File(home, ".cache")

	override fun userLogs(): File? = null

	// --

	override fun userDocuments() = fromEnv("XDG_DOCUMENTS_DIR") ?: File(home, "Documents")

	override fun userDownloads() = fromEnv("XDG_DOWNLOAD_DIR") ?: File(home, "Downloads")

	// --

	@Suppress("NOTHING_TO_INLINE")
	private inline fun fromEnv(name: String): File? {
		val v = System.getenv(name)
		if (!v.isNullOrEmpty()) {
			return File(v).absoluteFile
		}
		return null
	}
}
