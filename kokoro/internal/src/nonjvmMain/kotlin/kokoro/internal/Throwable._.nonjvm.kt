package kokoro.internal

/**
 * @see Throwable.addSuppressed
 */
@Suppress("NOTHING_TO_INLINE")
actual inline fun Throwable.addSuppressed_(exception: Throwable): Unit = addSuppressed(exception)

actual fun Throwable.throwAnySuppressed() {
	val suppressed = suppressedExceptions
	if (suppressed.isEmpty()) return

	val ex = suppressed[0]
	for (i in 1..<suppressed.size) {
		ex.addSuppressed(suppressed[i])
	}
	throw ex
}
