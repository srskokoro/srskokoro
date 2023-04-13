@file:Suppress("FunctionName", "unused")

package kokoro.internal.kotlin

@DslMarker
internal annotation class TODOMarker

@TODOMarker
internal object TODO {

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

internal inline fun <R> TODO(placeholder: TODO.() -> R) = placeholder(TODO)
