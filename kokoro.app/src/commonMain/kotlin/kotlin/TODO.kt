@file:Suppress("FunctionName", "unused")

package kotlin

@DslMarker
internal annotation class TODOMarker

@TODOMarker
internal object TODO {

	inline val IMPLEMENT: Nothing get() = IMPLEMENT()

	inline fun IMPLEMENT(): Nothing = throw NotImplementedError("TODO Implement")

	inline val NOP get() = NOP()

	inline fun NOP() = Unit

	/** Same as [NOP] */
	inline val PLACEHOLDER get() = PLACEHOLDER()

	/** Same as [NOP] */
	inline fun PLACEHOLDER() = Unit

	inline fun <R> PLACEHOLDER(block: TODO.() -> R) = block(TODO)
}

internal inline fun <R> TODO(placeholder: TODO.() -> R) = placeholder(TODO)
