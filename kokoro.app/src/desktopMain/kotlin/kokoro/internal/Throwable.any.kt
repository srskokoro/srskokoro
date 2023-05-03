package kokoro.internal

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.any(noinline predicate: (Throwable) -> Boolean) =
	any(ThrowableDejaVuSet(), predicate)

fun Throwable.any(dejaVu: ThrowableDejaVuSet = ThrowableDejaVuSet(), predicate: (Throwable) -> Boolean): Boolean {
	if (dejaVu.add(this)) {
		if (predicate(this))
			return true

		for (ex in suppressed) {
			if (ex.any(dejaVu, predicate))
				return true
		}

		try {
			cause
		} catch (ex: Throwable) {
			// Ignore. Let an ensuing `printStackTrace()` discover it instead.
			return false
		}?.let { cause ->
			return cause.any(dejaVu, predicate)
		}
	}
	return false
}
