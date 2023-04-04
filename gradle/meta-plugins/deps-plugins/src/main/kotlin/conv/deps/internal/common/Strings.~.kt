package conv.deps.internal.common

/**
 * Similar to [String.take] but throws if not enough characters are taken.
 * Identical to [String.substring]`(0, n)`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.first(n: Int): String {
	return substring(0, n)
}

/**
 * Similar to [String.drop] but throws if not enough characters are removed.
 * Identical to [String.substring]`(n)`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.remove(n: Int): String {
	return substring(n)
}

/**
 * Similar to [String.dropLast] but throws if not enough characters are removed.
 * Identical to [String.substring]`(0, length - n)`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.removeLast(n: Int): String {
	return substring(0, length - n)
}
