package kokoro.app.ui.wv

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@DslMarker
annotation class ArgumentsBuilderDsl

@Suppress("NOTHING_TO_INLINE")
@ArgumentsBuilderDsl
@JvmInline
value class ArgumentsBuilder(@PublishedApi internal val out: StringBuilder) {

	inline fun arg(value: Boolean): ArgumentsBuilder {
		out.append(value)
		out.append(',')
		return this
	}

	inline fun arg(value: Byte) = arg { append(value) }
	inline fun arg(value: Short) = arg { append(value) }
	inline fun arg(value: Int) = arg { append(value) }
	inline fun arg(value: Long) = arg { append(value) }
	inline fun arg(value: Float) = arg { append(value) }
	inline fun arg(value: Double) = arg { append(value) }

	inline fun arg(value: Char) = arg { append(value) }
	inline fun arg(value: CharArray) = arg { append(value) }
	inline fun arg(value: CharSequence) = arg { append(value) }
	inline fun arg(value: String) = arg { append(value) }

	inline fun arg(value: CharArray, startIndex: Int, endIndex: Int = value.size) = arg { append(value, startIndex, endIndex) }
	inline fun arg(value: CharSequence, startIndex: Int, endIndex: Int = value.length) = arg { append(value, startIndex, endIndex) }

	@OptIn(ExperimentalContracts::class)
	inline fun arg(block: Arg.() -> Arg): ArgumentsBuilder {
		contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
		block(Arg(out))
		out.append(',')
		return this
	}

	// --

	internal fun conclude() {
		if (out.isNotEmpty()) {
			val lastIndex = out.length - 1
			if (out[lastIndex] == ',') {
				out.setLength(lastIndex)
				return
			}
		}
		throw E_AlreadyConcluded()
	}

	internal fun conclude(closing: Char) {
		if (out.isNotEmpty()) {
			val lastIndex = out.length - 1
			if (out[lastIndex] == ',') {
				out[lastIndex] = closing
				return
			}
		}
		throw E_AlreadyConcluded()
	}

	internal fun conclude(closing: String) {
		if (out.isNotEmpty()) {
			val lastIndex = out.length - 1
			if (out[lastIndex] == ',') {
				out.setRange(lastIndex, lastIndex + 1, closing)
				return
			}
		}
		throw E_AlreadyConcluded()
	}

	// --

	@ArgumentsBuilderDsl
	@JvmInline
	value class Arg(@PublishedApi internal val out: StringBuilder) {

		inline fun append(value: Byte): Arg {
			// NOTE: Even if the following is `StringBuilder.append(Any?)`,
			// it'll actually resolve to `StringBuilder.append(Byte)` when
			// available in the target platform.
			out.append(value)
			return this
		}

		inline fun append(value: Short): Arg {
			// NOTE: Even if the following is `StringBuilder.append(Any?)`,
			// it'll actually resolve to `StringBuilder.append(Short)` when
			// available in the target platform.
			out.append(value)
			return this
		}

		inline fun append(value: Int): Arg {
			// NOTE: Even if the following is `StringBuilder.append(Any?)`,
			// it'll actually resolve to `StringBuilder.append(Int)` when
			// available in the target platform.
			out.append(value)
			return this
		}

		inline fun append(value: Long): Arg {
			// NOTE: Even if the following is `StringBuilder.append(Any?)`,
			// it'll actually resolve to `StringBuilder.append(Long)` when
			// available in the target platform.
			out.append(value)
			return this
		}

		inline fun append(value: Float): Arg {
			// NOTE: Even if the following is `StringBuilder.append(Any?)`,
			// it'll actually resolve to `StringBuilder.append(Float)` when
			// available in the target platform.
			out.append(value)
			return this
		}

		inline fun append(value: Double): Arg {
			// NOTE: Even if the following is `StringBuilder.append(Any?)`,
			// it'll actually resolve to `StringBuilder.append(Double)` when
			// available in the target platform.
			out.append(value)
			return this
		}

		inline fun append(value: Char): Arg {
			out.append(value)
			return this
		}

		inline fun append(value: CharArray): Arg {
			out.append(value)
			return this
		}

		inline fun append(value: CharSequence): Arg {
			out.append(value)
			return this
		}

		inline fun append(value: String): Arg {
			out.append(value)
			return this
		}

		// --

		inline fun append(value: CharArray, startIndex: Int, endIndex: Int = value.size): Arg {
			out.appendRange(value, startIndex, endIndex)
			return this
		}

		inline fun append(value: CharSequence, startIndex: Int, endIndex: Int = value.length): Arg {
			out.append(value, startIndex, endIndex)
			return this
		}

		// --

		inline operator fun Char.unaryPlus() = append(this)
		inline operator fun CharArray.unaryPlus() = append(this)
		inline operator fun CharSequence.unaryPlus() = append(this)
		inline operator fun String.unaryPlus() = append(this)

		inline operator fun plus(value: Byte) = append(value)
		inline operator fun plus(value: Short) = append(value)
		inline operator fun plus(value: Int) = append(value)
		inline operator fun plus(value: Long) = append(value)
		inline operator fun plus(value: Float) = append(value)
		inline operator fun plus(value: Double) = append(value)

		inline operator fun plus(value: Char) = append(value)
		inline operator fun plus(value: CharArray) = append(value)
		inline operator fun plus(value: CharSequence) = append(value)
		inline operator fun plus(value: String) = append(value)
	}
}

private fun E_AlreadyConcluded() = IllegalStateException("Already concluded")
