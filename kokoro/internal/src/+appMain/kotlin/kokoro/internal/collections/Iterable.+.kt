package kokoro.internal.collections

/**
 * @see Iterable.forEach
 */
inline fun <T> Iterable<T>.forEachDeferringThrow(action: (T) -> Unit) {
	var thrown: Throwable? = null
	for (element in this) {
		try {
			action(element)
		} catch (ex: Throwable) {
			if (thrown == null) thrown = ex
			else thrown.addSuppressed(ex)
		}
	}
	if (thrown != null) throw thrown
}

/**
 * @see Iterable.forEachIndexed
 */
inline fun <T> Iterable<T>.forEachIndexedDeferringThrow(action: (index: Int, T) -> Unit) {
	var thrown: Throwable? = null
	var i = 0
	for (element in this) {
		try {
			action(i++, element)
		} catch (ex: Throwable) {
			if (thrown == null) thrown = ex
			else thrown.addSuppressed(ex)
		}
	}
	if (thrown != null) throw thrown
}
