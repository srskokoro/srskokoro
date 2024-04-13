package kokoro.internal

/**
 * @see java.lang.Throwable.addSuppressed
 */
@Suppress("NOTHING_TO_INLINE")
actual inline fun Throwable.addSuppressed_(exception: Throwable) {
	@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
	(this as java.lang.Throwable).addSuppressed(exception)
}

actual fun Throwable.throwAnySuppressed() {
	val suppressed = suppressed
	if (suppressed.isEmpty()) return

	val ex = suppressed[0]
	for (i in 1..<suppressed.size) {
		ex.addSuppressed_(suppressed[i])
	}
	throw ex
}
