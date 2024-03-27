package build.api.file

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import java.io.File

/** @see FileSystemLocation.getAsFile */
val FileSystemLocation.file: File
	inline get() = asFile

val Provider<out FileSystemLocation>.file: File
	inline get() = get().file

val Provider<out FileSystemLocation>.fileOrNull: File?
	inline get() = orNull?.file
