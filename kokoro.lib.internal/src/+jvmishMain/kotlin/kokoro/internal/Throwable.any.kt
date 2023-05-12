package kokoro.internal

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.any(noinline predicate: (Throwable) -> Boolean) = any(ThrowableDejaVuSet(), predicate)

fun Throwable.any(dejaVu: ThrowableDejaVuSet = ThrowableDejaVuSet(), predicate: (Throwable) -> Boolean): Boolean {
	if (dejaVu.set.add(this)) {
		if (predicate(this))
			return true

		for (sx in suppressed) {
			if (sx.any(dejaVu, predicate))
				return true
		}

		try {
			cause
		} catch (ex: Throwable) {
			// Ignore. Let `printStackTrace()` discover it instead.
			return false
		}?.let { cause ->
			return cause.any(dejaVu, predicate)
		}
	}
	return false
}

fun Throwable.anyError() = any { it is Error }