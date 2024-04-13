package kokoro.internal.collections

import kokoro.internal.addSuppressed_
import kokoro.internal.assert

/**
 * Iterates through a [List] using the index and calls [action] for each item.
 * This does not allocate an iterator like [Iterable.forEach].
 *
 * WARNING: The [List] must implement [RandomAccess].
 */
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
	assert({ this is RandomAccess })
	for (i in indices) {
		val element = get(i)
		action(element)
	}
}

/**
 * Iterates through a [List] using the index and calls [action] for each item.
 * This does not allocate an iterator like [Iterable.forEachIndexed].
 *
 * WARNING: The [List] must implement [RandomAccess].
 */
inline fun <T> List<T>.fastForEachIndexed(action: (index: Int, T) -> Unit) {
	assert({ this is RandomAccess })
	for (i in indices) {
		val element = get(i)
		action(i, element)
	}
}

/**
 * @see fastForEach
 */
inline fun <T> List<T>.fastForEachDeferringThrow(action: (T) -> Unit) {
	assert({ this is RandomAccess })
	var thrown: Throwable? = null
	for (i in indices) {
		try {
			val element = get(i)
			action(element)
		} catch (ex: Throwable) {
			if (thrown == null) thrown = ex
			else thrown.addSuppressed_(ex)
		}
	}
	if (thrown != null) throw thrown
}

/**
 * @see fastForEachIndexed
 */
inline fun <T> List<T>.fastForEachIndexedDeferringThrow(action: (index: Int, T) -> Unit) {
	assert({ this is RandomAccess })
	var thrown: Throwable? = null
	for (i in indices) {
		try {
			val element = get(i)
			action(i, element)
		} catch (ex: Throwable) {
			if (thrown == null) thrown = ex
			else thrown.addSuppressed_(ex)
		}
	}
	if (thrown != null) throw thrown
}
