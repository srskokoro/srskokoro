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
	var outputModMs = source.lastModified()

	// NOTE:
	// - `lastModified()` is `0L` for nonexistent files.
	// - We don't care if the source file doesn't really exist.
	// - We manually timestamp the destination file to have the same
	// modification time as the source file at the time of generation.
	// - If the timestamp is zero, then that should be interpreted as a request
	// for forced (re)generation, even if the destination file already exists.
	if (destination.lastModified() == outputModMs && outputModMs != 0L) {
		return false // The destination file is likely up-to-date!
	} else {
		// The destination file likely needs (re)generation!
	}

	// NOTE: The following handles an edge case where the source file gets
	// modified yet still have the same timestamp as before, simply because the
	// wall-clock time is the same as the source file's current timestamp.
	if (outputModMs >= System.currentTimeMillis()) {
		// At this point, the source file might have been modified while we're
		// already running (or the source file's timestamp may have been
		// maliciously set to the future). Anyway, force regeneration of the
		// destination file after the current (re)generation.
		outputModMs = 0L
	}

	// --

	val tmp = transformFileAtomic_initTmp(destination)
	try {
		transformFileAtomic_initFc(tmp).use {
			generator.invoke(it)
		}
		transformFileAtomic_finish(outputModMs, tmp, destination)
	} catch (ex: Throwable) {
		tmp.delete()
		throw ex
	}

	return true
}


@PublishedApi
internal fun transformFileAtomic_initTmp(destination: File): File {
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
internal fun transformFileAtomic_finish(outputModMs: Long, tmp: File, destination: File) {
	// Set up modification time manually for our custom up-to-date check
	tmp.setLastModified(outputModMs)

	// Atomically publish our changes via a rename/move operation
	Files.move(tmp.toPath(), destination.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
}