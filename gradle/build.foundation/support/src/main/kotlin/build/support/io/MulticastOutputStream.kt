package build.support.io

import java.io.OutputStream

class MulticastOutputStream(
	vararg streams: OutputStream,
) : OutputStream() {

	private val streams_ = streams

	val streams = streams.asList()

	override fun toString() = streams.toString()

	// --

	override fun write(b: Int) {
		for (s in streams_) s.write(b)
	}

	override fun write(b: ByteArray) {
		for (s in streams_) s.write(b)
	}

	override fun write(b: ByteArray, off: Int, len: Int) {
		for (s in streams_) s.write(b, off, len)
	}

	override fun flush() {
		for (s in streams_) s.flush()
	}

	override fun close() {
		for (s in streams_) s.close()
	}
}
