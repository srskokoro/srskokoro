package build.support.io

import java.io.File
import java.nio.file.Files

/**
 * Just like Kotlin's [File.resolve], except that we trust this more :P
 */
fun File.safeResolve(relative: String): File {
	val r = File(relative)
	return if (r.isAbsolute) r
	else File(this, relative)
}

/** @see safeResolve */
fun File.safeResolve(relative: File): File {
	@Suppress("UnnecessaryVariable")
	val r = relative
	return if (r.isAbsolute) r
	else File(this, r.path)
}

@Suppress("NOTHING_TO_INLINE")
inline fun File.isEmptyDirectory() = toPath().isEmptyDirectory()

@Suppress("NOTHING_TO_INLINE")
inline fun File.isNonEmptyDirectory() = toPath().isNonEmptyDirectory()

fun File.initDirs(): File {
	if (!mkdirs() && !isDirectory) {
		Files.createDirectories(toPath()) // Let this throw
	}
	return this
}

fun File.initParentDirs(): File {
	parentFile.initDirs()
	return this
}

fun File.clean() {
	for (child in listFiles() ?: return)
		if (!child.deleteRecursively()) throw AccessDeniedException(child, reason = "Deletion failed")
}
