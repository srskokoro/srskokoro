package kokoro.internal.io

import okio.Buffer
import okio.Sink
import okio.Timeout

/**
 * Similar to [okio.blackholeSink]`()` but always provides the same instance.
 *
 * @see VoidSource
 */
data object VoidSink : Sink {
	override fun write(source: Buffer, byteCount: Long) = source.skip(byteCount)
	override fun flush() {}
	override fun timeout() = Timeout.NONE
	override fun close() {}
}
