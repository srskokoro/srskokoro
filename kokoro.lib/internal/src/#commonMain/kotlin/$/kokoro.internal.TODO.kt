@file:Suppress("PackageDirectoryMismatch", "unused", "NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

@DslMarker
private annotation class TODOMarker

@TODOMarker
object TODO {

	inline val IMPLEMENT: Nothing get() = IMPLEMENT()

	fun IMPLEMENT(): Nothing = throw NotImplementedError("TODO Implement")

	fun IMPLEMENT(feature: String): Nothing = if (feature.isEmpty()) IMPLEMENT() else
		throw NotImplementedError("TODO Implement: $feature")

	inline val NOP get() = NOP()

	inline fun NOP() = Unit

	/** Same as [IMPLEMENT] */
	inline fun <R> PLACEHOLDER(): R = IMPLEMENT()

	inline fun <R> PLACEHOLDER(block: TODO.() -> R) = block(TODO)
}

inline fun <R> TODO(placeholder: TODO.() -> R) = placeholder(TODO)
