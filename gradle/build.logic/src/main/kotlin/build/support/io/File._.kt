package build.support.io

import java.io.File
import java.nio.file.Files

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
