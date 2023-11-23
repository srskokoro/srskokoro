package kokoro.app

import kokoro.internal.assert
import kokoro.internal.io.NioPath
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.*

actual fun AppData.findCollectionsDirs(): List<Path> {
	val r = mutableListOf<Path>()

	val home = File(System.getProperty("user.home")).canonicalFile
	val lookupFile = getCollectionsDirsLookupFile()

	if (lookupFile.exists()) lookupFile.source().buffer().use {
		while (true) {
			val line = it.readUtf8Line() ?: break

			// Ignore comment lines
			if (line.startsWith('#')) continue

			@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
			if ((line as java.lang.String).isBlank) continue

			val entry = try {
				getCanonicalPath(home, line)
			} catch (ex: IOException) {
				ex.addSuppressed(IOException("ERROR Line: $line"))
				ex.printStackTrace()
				it.close() // We're about to write to the file
				generateCollectionsDirsLookupFile(lookupFile)
				return findCollectionsDirs() // Try again!
			}

			r.add(entry.toPath())
		}
	}

	if (r.isEmpty()) {
		generateCollectionsDirsLookupFile(lookupFile)
		r.add(getDefaultCollectionsDir(home))
	}

	return r
}

private fun AppData.getCollectionsDirsLookupFile() =
	File(mainDir.toString() + File.separatorChar + "cols.lst")

private fun getCanonicalPath(parent: File, child: String): String {
	var f = File(child)
	if (!f.isAbsolute) f = File(parent, child)
	return f.canonicalPath
}

private fun getDefaultCollectionsDir(home: File): Path {
	return (System.getenv("SRS_KOKORO_COLLECTIONS_DEFAULT")?.let {
		getCanonicalPath(home, it)
	} ?: buildString(64) {
		append(home)
		append(File.separatorChar)

		append("Documents")
		append(File.separatorChar)

		@Suppress("KotlinConstantConditions")
		assert({ "Must avoid potential clash (in case they end up being stored under the same parent directory)" }) {
			AppBuildDesktop.USER_COLLECTIONS_DIR_NAME != AppBuildDesktop.APP_DATA_DIR_NAME
		}
		append(AppBuildDesktop.USER_COLLECTIONS_DIR_NAME)
	}).toPath()
}

private fun generateCollectionsDirsLookupFile(lookupFile: File) {
	val validLines = mutableListOf<String>()
	var hasValidPath = false

	val home = File(System.getProperty("user.home")).canonicalFile
	if (lookupFile.exists()) lookupFile.source().buffer().use {
		while (true) {
			val line = it.readUtf8Line() ?: break

			@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
			if (!line.startsWith('#') && !(line as java.lang.String).isBlank) try {
				getCanonicalPath(home, line)
				hasValidPath = true
			} catch (_: IOException) {
				validLines.add("#!!ERROR: $line")
				continue
			}
			validLines.add(line)
		}
	}

	if (!hasValidPath) {
		val entry = getDefaultCollectionsDir(home).toString()
		validLines.add(entry)
	}

	// Atomically generate a file, by writing to a temporary first, followed by
	// an atomic rename/replace.
	val tmpPath = NioPath.of("$lookupFile.tmp")
	Files.newOutputStream(tmpPath, DSYNC, CREATE, WRITE, TRUNCATE_EXISTING).sink().buffer().use {
		for (line in validLines) {
			it.writeUtf8(line)
			it.writeByte('\n'.code)
		}
	}
	// Atomically publish our changes via a rename/move operation
	Files.move(tmpPath, NioPath.of(lookupFile.path), ATOMIC_MOVE, REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
}
