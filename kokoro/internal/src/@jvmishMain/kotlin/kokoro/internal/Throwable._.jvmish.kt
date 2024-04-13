package kokoro.internal

actual fun Throwable.throwAnySuppressed() {
	val suppressed = suppressed
	if (suppressed.isEmpty()) return

	val ex = suppressed[0]
	for (i in 1..<suppressed.size) {
		@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
		(ex as java.lang.Throwable).addSuppressed(suppressed[i])
	}
	throw ex
}
