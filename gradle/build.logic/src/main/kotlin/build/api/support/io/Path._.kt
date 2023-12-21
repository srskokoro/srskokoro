package build.api.support.io

import java.nio.file.Files
import java.nio.file.Path

@Suppress("NOTHING_TO_INLINE")
inline fun Path.isEmptyDirectory() = !isNonEmptyDirectory()

fun Path.isNonEmptyDirectory(): Boolean {
	Files.newDirectoryStream(this).use {
		return it.iterator().hasNext()
	}
}
