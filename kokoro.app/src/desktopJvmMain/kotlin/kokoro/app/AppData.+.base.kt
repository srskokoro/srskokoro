package kokoro.app

import assert
import kokoro.internal.io.NioPath
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.StandardCopyOption.*
import java.nio.file.StandardOpenOption.*

actual fun AppData.findCollectionsDirs(): List<Path> {
	val r = mutableListOf<Path>()

	var hasBadEntries = false
	val lookupFile = File(mainDir.toString() + File.separatorChar + "cols.lst")
	if (lookupFile.exists()) lookupFile.useLines { seq ->
		for (line in seq) {
			if (line.startsWith('#')) continue

			@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
			if ((line as java.lang.String).isBlank) continue

			val entry = try {
				File(line).canonicalPath
			} catch (ex: IOException) {
				ex.addSuppressed(IOException("Input path: $line"))
				ex.printStackTrace()
				hasBadEntries = true
				continue
			}

			r.add(entry.toPath())
		}
	}

	if (hasBadEntries) {
		// Regenerate file without the bad entries.

		// Atomically generate a file, by writing to a temporary first, followed
		// by an atomic rename.
		val lookupFileStr = lookupFile.path
		NioPath.of("${lookupFileStr}.tmp").let { tmp ->
			Files.newOutputStream(tmp, DSYNC, CREATE, WRITE, TRUNCATE_EXISTING).sink().buffer().use { out ->
				for (it in r) out.writeUtf8(it.toString())
			}
			// Atomically publish our changes via a rename/move operation
			Files.move(tmp, NioPath.of(lookupFileStr), ATOMIC_MOVE, REPLACE_EXISTING)
			// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
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

		// Atomically edits file, by making a backup copy first, editing that
		// copy, then finally doing an atomic rename/replace.
		val lookupFileStr = lookupFile.path
		val lookupPath = NioPath.of(lookupFileStr)
		NioPath.of("${lookupFileStr}.tmp").let { tmp ->
			if (lookupFile.isFile) Files.copy(lookupPath, tmp, REPLACE_EXISTING, COPY_ATTRIBUTES, NOFOLLOW_LINKS)
			val nonEmpty = lookupFile.length() > 0
			Files.newOutputStream(tmp, DSYNC, CREATE, WRITE, APPEND).sink().buffer().use { out ->
				if (nonEmpty) out.writeByte('\n'.code)
				out.writeUtf8(entry)
				out.writeByte('\n'.code)
			}
			// Atomically publish our changes via a rename/move operation
			Files.move(tmp, lookupPath, ATOMIC_MOVE, REPLACE_EXISTING)
			// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
		}
	}

	return r
}
