package build.support.io

import java.io.File

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
