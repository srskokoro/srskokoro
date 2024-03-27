package build.api.file

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider

val FileSystemLocation.path: String
	inline get() = asFile.path

val Provider<out FileSystemLocation>.path: String
	inline get() = get().path

val Provider<out FileSystemLocation>.pathOrNull: String?
	inline get() = orNull?.path
