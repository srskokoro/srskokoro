package build.api.support

import java.io.File
import java.nio.file.Path
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
inline fun getFileUri(path: String) = getFileUri(Path.of(path))

@Suppress("NOTHING_TO_INLINE")
inline fun getFileUri(path: File) = getFileUri(path.toPath())

fun getFileUri(path: Path): String {
	// See, https://stackoverflow.com/questions/46610910
	return path.toUri().toASCIIString()
}

@Suppress("NOTHING_TO_INLINE")
inline fun CharSequence.lineInfoUriAt(index: Int, path: String) = lineInfoUriAt(index, Path.of(path))

@Suppress("NOTHING_TO_INLINE")
inline fun CharSequence.lineInfoUriAt(index: Int, path: File) = lineInfoUriAt(index, path.toPath())

fun CharSequence.lineInfoUriAt(index: Int, path: Path) =
	lineInfoAt(index).let { (ln, col) -> "${getFileUri(path)}:$ln:$col" }

fun CharSequence.lineInfoStrAt(index: Int): String =
	lineInfoAt(index).let { (ln, col) -> "$ln:$col" }

fun CharSequence.lineInfoAt(index: Int): Pair<Int, Int> {
	val n = min(max(index, 0), length)
	var cr = 0
	var lf = 0
	var lastLine_i = -1
	for (i in 0 until n) {
		when (this[i]) {
			'\n' -> lf++
			'\r' -> cr++
			else -> continue
		}
		lastLine_i = i
	}
	val ln = max(cr, lf) + 1
	val col = n - lastLine_i
	return ln to col
}
