package build.support.io

import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.*

inline fun transformFileAtomic(
	source: File,
	destination: File,
	generator: (FileChannel) -> Unit,
): Boolean {
	val sourceModMs = source.lastModified()
	val tmp = transformFileAtomic_init(sourceModMs, destination)
		?: return false // It's likely up-to-date!

	try {
		transformFileAtomic_initFc(tmp).use {
			generator.invoke(it)
		}
		transformFileAtomic_finish(sourceModMs, tmp, destination)
	} catch (ex: Throwable) {
		tmp.delete()
		throw ex
	}

	return true
}

@PublishedApi
internal fun transformFileAtomic_init(
	sourceModMs: Long,
	destination: File,
): File? {
	destination.lastModified().let {
		// Check if the destination file doesn't exist yet
		if (it == 0L && !destination.isFile) return@let

		// NOTE: We manually timestamp the destination file to have the same
		// modification time as the source file at the time of generation.
		// - Also, we don't care if the source file doesn't really exist.
		if (it != sourceModMs) return@let

		return null // The destination file is likely up-to-date!
	}

	val tmp = File("$destination.tmp")
	if (!tmp.delete()) tmp.parentFile.mkdirs()

	return tmp
}

@PublishedApi
internal fun transformFileAtomic_initFc(tmp: File): FileChannel {
	// Needs `SYNC` here since otherwise, file writes and metadata (especially,
	// modification time) can be delayed by the OS (even on properly closed
	// streams) and we have to do a rename/move operation later to atomically
	// publish our changes.
	return FileChannel.open(tmp.toPath(), SYNC, CREATE, TRUNCATE_EXISTING, WRITE)
}

@PublishedApi
internal fun transformFileAtomic_finish(sourceModMs: Long, tmp: File, destination: File) {
	// Set up modification time manually for our custom up-to-date check
	tmp.setLastModified(sourceModMs)

	// Atomically publish our changes via a rename/move operation
	Files.move(tmp.toPath(), destination.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
}
