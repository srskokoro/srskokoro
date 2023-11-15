package kokoro.internal.io

import okio.Buffer
import okio.Sink
import okio.Source
import okio.Timeout

fun nullSource(): Source = NullSource

private object NullSource : Source {
	override fun read(sink: Buffer, byteCount: Long): Long = -1L
	override fun timeout(): Timeout = Timeout.NONE
	override fun close() {}
}

/**
 * Similar to [okio.blackholeSink]`()` but always returns the same instance.
 */
fun nullSink(): Sink = NullSink

private object NullSink : Sink {
	override fun write(source: Buffer, byteCount: Long) = source.skip(byteCount)
	override fun flush() {}
	override fun timeout() = Timeout.NONE
	override fun close() {}
}
