package build.support

/**
 * Efficiently computes the following:
 * ```
 * (this + other).hashCode()
 * ```
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.hashCodeWith(other: String): Int {
	// The following computes the equivalent of `(this + other).hashCode()`, but
	// without actually concatenating the strings.
	// - Possible because `java.lang.String.hashCode()` is stable and set in
	// stone according to its docs -- see, https://stackoverflow.com/a/785150
	//   - The Java compiler even uses that fact to implement switches-over-
	//   strings. See, https://github.com/ndru83/desugaring-java/blob/master/switch-case-internals.adoc
	// - See also, “Are fixed-width integers distributive over multiplication?”
	// on Stack Overflow -- https://stackoverflow.com/q/14189299
	return hashCode() * fastPow31(other.length) + other.hashCode()
}

@Suppress("NOTHING_TO_INLINE")
inline fun Int.hashCodeWith(other: String): Int {
	return this * fastPow31(other.length) + other.hashCode()
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.hashCodeWith(other: Char): Int {
	return hashCode() * 31 + other.code
}

@Suppress("NOTHING_TO_INLINE")
inline fun Int.hashCodeWith(other: Char): Int {
	return this * 31 + other.code
}

@Suppress("NOTHING_TO_INLINE")
inline fun Char.hashCodeWith(other: String): Int {
	return code * fastPow31(other.length) + other.hashCode()
}

@Suppress("NOTHING_TO_INLINE")
inline fun Char.hashCodeWith(other: Char): Int {
	return code * 31 + other.code
}
