package conv.deps.internal.common

import java.io.File

/**
 * Just like Kotlin's [File.resolve], except that we trust this more :P
 */
internal fun File.safeResolve(relative: String): File {
	val r = File(relative)
	return if (r.isAbsolute) r
	else File(this, relative)
}

/** @see safeResolve */
internal fun File.safeResolve(relative: File): File {
	@Suppress("UnnecessaryVariable")
	val r = relative
	return if (r.isAbsolute) r
	else File(this, r.path)
}
