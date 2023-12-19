package build.support.io

import java.io.CharArrayWriter
import java.io.Writer
import java.nio.CharBuffer
import java.util.Arrays
import kotlin.math.max
import kotlin.math.min

class UnsafeCharArrayWriter : CharArrayWriter {

	constructor() : super()

	constructor(initialSize: Int) : super(initialSize)

	override fun write(value: Int) {
		val newCount = count + 1
		if (newCount > buf.size) {
			buf = Arrays.copyOf(buf, max(buf.size shl 1, newCount))
		}
		buf[count] = value.toChar()
		count = newCount
	}

	override fun write(value: CharArray, offset: Int, length: Int) {
		if ((offset < 0) || (offset > value.size) || (length < 0) || ((offset + length) > value.size) || ((offset + length) < 0)) {
			throw IndexOutOfBoundsException()
		} else if (length == 0) {
			return
		}
		val newCount = count + length
		if (newCount > buf.size) {
			buf = Arrays.copyOf(buf, max(buf.size shl 1, newCount))
		}
		System.arraycopy(value, offset, buf, count, length)
		count = newCount
	}

	override fun write(value: String, offset: Int, length: Int) {
		val newCount = count + length
		if (newCount > buf.size) {
			buf = Arrays.copyOf(buf, max(buf.size shl 1, newCount))
		}
		value.toCharArray(buf, count, offset, offset + length)
		count = newCount
	}

	override fun writeTo(out: Writer) {
		out.write(buf, 0, count)
	}

	fun truncate(size: Int) {
		count = min(size, count)
	}

	fun getUnsafeCharArray(): CharArray = buf

	fun getUnsafeCharBuffer(): CharBuffer = CharBuffer.wrap(buf, 0, count)

	override fun toCharArray(): CharArray = Arrays.copyOf(buf, count)

	override fun toString() = String(buf, 0, count)
}
