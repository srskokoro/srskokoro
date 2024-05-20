package kokoro.internal.io

import okio.Buffer
import okio.ByteString.Companion.toByteString
import okio.Source
import okio.Timeout
import kotlin.jvm.JvmField
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.source() = ByteArraySource(this)

class ByteArraySource(
	@JvmField val data: ByteArray,
) : Source {
	private var pos = 0

	override fun read(sink: Buffer, byteCount: Long): Long {
		require(byteCount >= 0L) { "byteCount < 0: $byteCount" }

		val o = pos
		val d = data

		val n = min(d.size - o, byteCount.toInt())
		if (n <= 0) return -1L

		sink.write(d, o, n)
		pos = o + n

		return n.toLong()
	}

	override fun timeout(): Timeout = Timeout.NONE

	override fun close() {}

	override fun toString() = "source(${data.toByteString()})"
}
