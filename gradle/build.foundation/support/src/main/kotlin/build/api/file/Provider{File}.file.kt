package build.api.file

import org.gradle.api.provider.Provider
import java.io.File

val Provider<out File>.file: File
	inline get() = get()

val Provider<out File>.fileOrNull: File?
	inline get() = orNull
