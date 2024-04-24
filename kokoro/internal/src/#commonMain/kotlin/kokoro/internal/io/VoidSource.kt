package kokoro.internal.io

import okio.Buffer
import okio.Source
import okio.Timeout

/**
 * @see VoidSink
 */
data object VoidSource : Source {
	override fun read(sink: Buffer, byteCount: Long): Long = -1L
	override fun timeout(): Timeout = Timeout.NONE
	override fun close() {}
}
