package build.api.file

import org.gradle.api.provider.Provider
import java.io.File

val Provider<out File>.path: String
	inline get() = get().path

val Provider<out File>.pathOrNull: String?
	inline get() = orNull?.path
