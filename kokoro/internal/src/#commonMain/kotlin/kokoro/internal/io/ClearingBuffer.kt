package kokoro.internal.io

import okio.Buffer
import okio.Sink
import okio.Source
import okio.Timeout
import kotlin.jvm.JvmField

@Suppress("NOTHING_TO_INLINE")
inline fun Buffer.asClearing() = ClearingBuffer(this)

/**
 * A [Source]/[Sink] that clears [buffer] on [close].
 *
 * @see Buffer.asClearing
 */
class ClearingBuffer(
	@JvmField val buffer: Buffer,
) : Source, Sink {

	override fun read(sink: Buffer, byteCount: Long): Long = buffer.read(sink, byteCount)

	override fun write(source: Buffer, byteCount: Long) = buffer.write(source, byteCount)

	override fun flush() = buffer.flush()

	override fun close(): Unit = with(buffer) {
		clear()
		close() // Presumably does nothing
	}

	override fun timeout(): Timeout = buffer.timeout()

	/** @see Buffer.asClearing */
	override fun toString() = "asClearing($buffer)"
}
