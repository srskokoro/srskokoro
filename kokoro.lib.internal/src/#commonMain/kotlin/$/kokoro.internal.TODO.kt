@file:Suppress("PackageDirectoryMismatch", "FunctionName", "unused", "NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

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

	/** Same as [NOP] */
	inline val PLACEHOLDER get() = PLACEHOLDER()

	/** Same as [NOP] */
	inline fun PLACEHOLDER() = Unit

	inline fun <R> PLACEHOLDER(block: TODO.() -> R) = block(TODO)
}

inline fun <R> TODO(placeholder: TODO.() -> R) = placeholder(TODO)
