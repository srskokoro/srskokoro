package kokoro.app

import assert
import kokoro.internal.DEBUG
import kokoro.internal.io.NioPath
import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.*

actual fun AppData.findCollectionsDirs(): List<Path> {
	val r = mutableListOf<Path>()

	val lookupFile = File(mainDir.toString() + File.separatorChar + "collections.lst")
	if (lookupFile.exists()) lookupFile.useLines { seq ->
		for (line in seq) {
			if (line.startsWith('#')) continue

			@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
			if ((line as java.lang.String).isBlank) continue

			val entry = try {
				File(line).canonicalPath
			} catch (ex: IOException) {
				ex.addSuppressed(IOException("Input path: $line"))
				if (DEBUG) throw ex
				ex.printStackTrace()
				continue
			}

			r.add(entry.toPath())
		}
	}

	if (r.isEmpty()) {
		// Generate a default entry
		val entry = System.getenv("SRS_KOKORO_COLLECTIONS_DEFAULT") ?: buildString(64) {
			append(System.getProperty("user.home"))
			append(File.separatorChar)

			append("Documents")
			append(File.separatorChar)

			@Suppress("KotlinConstantConditions")
			assert({ "Must avoid potential clash (in case they end up being stored under the same parent directory)" }) {
				AppBuildDesktop.USER_COLLECTIONS_DIR_NAME != AppBuildDesktop.APP_DATA_DIR_NAME
			}
			append(AppBuildDesktop.USER_COLLECTIONS_DIR_NAME)
		}
		r.add(entry.toPath())

		// Atomically generate a file, by writing to a temporary first, followed
		// by an atomic rename.
		val lookupFileStr = lookupFile.path
		NioPath.of("${lookupFileStr}.tmp").let { tmp ->
			Files.writeString(tmp, entry, DSYNC, CREATE, WRITE, TRUNCATE_EXISTING)
			// Atomically publish our changes via a rename/move operation
			Files.move(tmp, NioPath.of(lookupFileStr), ATOMIC_MOVE, REPLACE_EXISTING)
			// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
		}
	}

	return r
}
