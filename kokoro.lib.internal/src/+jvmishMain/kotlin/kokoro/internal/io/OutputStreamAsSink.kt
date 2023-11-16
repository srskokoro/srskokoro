package kokoro.internal.io

import okio.Sink
import okio.buffer
import okio.sink
import java.io.OutputStream

fun OutputStream.asSink() = OutputStreamAsSink(this)

class OutputStreamAsSink(
	@PublishedApi @JvmField
	internal val outputStream: OutputStream,
) : Sink by outputStream.sink() {

	@Suppress("NOTHING_TO_INLINE")
	inline fun asOutputStream() = outputStream

	override fun toString() = "asSink($outputStream)"
}

fun Sink.asOutputStream(): OutputStream {
	if (this is OutputStreamAsSink) {
		return asOutputStream()
	} else {
		return buffer().outputStream()
	}
}
