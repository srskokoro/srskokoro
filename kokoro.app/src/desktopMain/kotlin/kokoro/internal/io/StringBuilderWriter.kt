package kokoro.internal.io

import java.io.Serializable
import java.io.Writer

/**
 * [Writer] implementation that outputs to a [StringBuilder].
 */
class StringBuilderWriter : Writer, Serializable {
	val builder: StringBuilder

	constructor() {
		builder = StringBuilder()
	}

	constructor(capacity: Int) {
		builder = StringBuilder(capacity)
	}

	constructor(builder: StringBuilder?) {
		this.builder = builder ?: StringBuilder()
	}

	/**
	 * Writes a single character.  The character to be written is contained in
	 * the 16 low-order bits of the given integer value; the 16 high-order bits
	 * are ignored.
	 *
	 * @param value the character to write.
	 */
	override fun write(value: Int) {
		builder.append(value.toChar())
	}

	/**
	 * Writes a character array.
	 *
	 * @param value the value to write.
	 */
	override fun write(value: CharArray) {
		builder.append(value)
	}

	/**
	 * Writes a portion of a character array.
	 *
	 * @param value the value to write.
	 * @param offset the index of the first character.
	 * @param length the number of characters to write.
	 */
	override fun write(value: CharArray, offset: Int, length: Int) {
		builder.append(value, offset, length)
	}

	/**
	 * Writes a [String].
	 *
	 * @param value the value to write.
	 */
	override fun write(value: String) {
		builder.append(value)
	}

	/**
	 * Writes a portion of a [String].
	 *
	 * @param value the value to write.
	 * @param offset the index of the first character.
	 * @param length the number of characters to write.
	 */
	override fun write(value: String, offset: Int, length: Int) {
		builder.append(value, offset, offset + length)
	}

	/**
	 * Appends a single character.
	 *
	 * @param value the character to append.
	 * @return this instance.
	 */
	override fun append(value: Char): StringBuilderWriter {
		builder.append(value)
		return this
	}

	/**
	 * Appends a character sequence.
	 *
	 * @param value the character sequence to append. If `null`, then the
	 * [value] is treated as if it was the string `"null"`.
	 * @return this instance.
	 */
	override fun append(value: CharSequence?): StringBuilderWriter {
		builder.append(value)
		return this
	}

	/**
	 * Appends a portion of a character sequence.
	 *
	 * @param value the character sequence from which a subsequence is appended.
	 * If `null`, then the [value] is treated as if it was the string `"null"`.
	 * @param startIndex the beginning (inclusive) of the subsequence to append.
	 * @param endIndex the end (exclusive) of the subsequence to append.
	 * @return this instance.
	 */
	override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): StringBuilderWriter {
		builder.append(value, startIndex, endIndex)
		return this
	}

	/**
	 * @return [StringBuilder.toString()][java.lang.StringBuilder.toString].
	 */
	override fun toString(): String {
		return builder.toString()
	}

	/** Flushing this `Writer` has no effect. */
	override fun flush() = Unit

	/** Closing this `Writer` has no effect. */
	override fun close() = Unit
}
