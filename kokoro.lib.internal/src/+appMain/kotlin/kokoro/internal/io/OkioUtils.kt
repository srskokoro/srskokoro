package kokoro.internal.io

import okio.Buffer
import okio.Sink
import okio.Source
import okio.Timeout

fun voidSource(): Source = VoidSource

private data object VoidSource : Source {
	override fun read(sink: Buffer, byteCount: Long): Long = -1L
	override fun timeout(): Timeout = Timeout.NONE
	override fun close() {}
}

/**
 * Similar to [okio.blackholeSink]`()` but always returns the same instance.
 */
fun voidSink(): Sink = VoidSink

private data object VoidSink : Sink {
	override fun write(source: Buffer, byteCount: Long) = source.skip(byteCount)
	override fun flush() {}
	override fun timeout() = Timeout.NONE
	override fun close() {}
}
