package kokoro.internal.io

import okio.BufferedSink
import kotlin.jvm.JvmField

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSink.asAppendableUtf8() = SinkAsAppendableUtf8(this)

class SinkAsAppendableUtf8(
	@JvmField val sink: BufferedSink,
) : Appendable {

	override fun append(value: Char): Appendable {
		sink.writeUtf8CodePoint(value.code)
		return this
	}

	override fun append(value: CharSequence?): Appendable {
		sink.writeUtf8(value.toString())
		return this
	}

	override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
		sink.writeUtf8(value.toString(), startIndex, endIndex)
		return this
	}

	/** @see BufferedSink.asAppendableUtf8 */
	override fun toString() = "asAppendableUtf8($sink)"
}
