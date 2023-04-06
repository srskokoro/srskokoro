package conv.deps.internal.common

/**
 * Same as [String.substring]`(start)` but useful for improving readability,
 * e.g., compare the following:
 * ```
 * val r1 = str.substring(s)
 * val r2 = str.from(s)
 * ```
 * Also, unlike [String.drop]`(start)`, this method throws if not enough
 * characters are removed.
 *
 * @see String.until
 * @see String.removeFirst
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.from(start: Int) = substring(start)

/**
 * Same as [String.substring]`(start, end)` but useful for improving
 * readability.
 *
 * @see String.from
 * @see String.until
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.from(start: Int, end: Int) = substring(start, end)

/**
 * Same as [String.substring]`(0, end)` but useful for improving readability,
 * e.g., compare the following:
 * ```
 * val r1 = str.substring(0, n)
 * val r2 = str.until(n)
 * ```
 * Also, unlike [String.take]`(end)`, this method throws if not enough
 * characters are available.
 *
 * @see String.from
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.until(end: Int) = substring(0, end)


/**
 * Same as [String.from]`(n)`.
 *
 * Similar to [String.drop] but throws if not enough characters are removed.
 * Identical to [String.substring]`(n)`.
 *
 * @see String.removeLast
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.removeFirst(n: Int) = from(n)

/**
 * Same as [String.until]`(length - n)`.
 *
 * Similar to [String.dropLast] but throws if not enough characters are removed.
 * Identical to [String.substring]`(0, length - n)`.
 *
 * @see String.removeFirst
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun String.removeLast(n: Int) = until(length - n)
