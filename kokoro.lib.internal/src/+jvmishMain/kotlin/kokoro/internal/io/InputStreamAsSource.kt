package kokoro.internal.io

import okio.Source
import okio.buffer
import okio.source
import java.io.InputStream

fun InputStream.asSource() = InputStreamAsSource(this)

class InputStreamAsSource(
	@PublishedApi @JvmField
	internal val inputStream: InputStream,
) : Source by inputStream.source() {

	@Suppress("NOTHING_TO_INLINE")
	inline fun asInputStream() = inputStream
}

fun Source.asInputStream(): InputStream {
	if (this is InputStreamAsSource) {
		return asInputStream()
	} else {
		return buffer().inputStream()
	}
}
