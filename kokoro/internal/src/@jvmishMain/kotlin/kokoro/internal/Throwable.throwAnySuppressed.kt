package kokoro.internal

fun Throwable.throwAnySuppressed() {
	val suppressed = suppressed
	if (suppressed.isEmpty()) return

	val ex = suppressed[0]
	for (i in 1..<suppressed.size) {
		ex.addSuppressed(suppressed[i])
	}
	throw ex
}
