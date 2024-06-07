package kokoro.internal.io

import okio.BufferedSink
import okio.Closeable
import okio.Sink
import okio.buffer
import kotlin.jvm.JvmField

/** @see SinkAppendable */
@Suppress("NOTHING_TO_INLINE")
inline fun Sink.asAppendable() = SinkAppendable(this)

/** @see SinkAppendable */
@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSink.asAppendable() = SinkAppendable(this)

/**
 * An [Appendable] that writes UTF-8 strings into [sink].
 *
 * @see BufferedSink.asAppendable
 * @see Sink.asAppendable
 */
class SinkAppendable(
	@JvmField val sink: BufferedSink,
) : Appendable, Closeable {

	constructor(sink: Sink) : this(if (sink is BufferedSink) sink else sink.buffer())

	@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
	override inline fun append(value: Char): SinkAppendable {
		sink.writeUtf8CodePoint(value.code)
		return this
	}

	@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
	override inline fun append(value: CharSequence?): SinkAppendable {
		sink.writeUtf8(value.toString())
		return this
	}

	@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
	override inline fun append(value: CharSequence?, startIndex: Int, endIndex: Int): SinkAppendable {
		sink.writeUtf8(value.toString(), startIndex, endIndex)
		return this
	}

	@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
	override inline fun close() = sink.close()

	/** @see BufferedSink.asAppendable */
	override fun toString() = "asAppendable($sink)"
}
