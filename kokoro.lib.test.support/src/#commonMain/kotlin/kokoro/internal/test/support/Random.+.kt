package kokoro.internal.test.support

import kotlin.random.Random

/** @see io.kotest.property.arbitrary.printableAscii */
val PRINTABLE_ASCII_CHARS = (' '..'~').toList()

/** @see io.kotest.property.arbitrary.ascii */
val ASCII_CHARS = ('\u0000'..'\u007F').toList()

val DIGIT_CHARS = ('0'..'9').toList()

@Suppress("NOTHING_TO_INLINE")
inline fun Random.nextString(length: Int) = nextString(ASCII_CHARS, length)

fun Random.nextString(charList: List<Char>, length: Int) = buildString {
	nextString(this, charList, length)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Random.nextString(out: StringBuilder, length: Int) =
	nextString(out, ASCII_CHARS, length)

fun Random.nextString(out: StringBuilder, charList: List<Char>, length: Int): StringBuilder {
	var n = length
	while (--n >= 0)
		out.append(charList.random(this))
	return out
}

fun Random.nextIntFavorSmall(until: Int): Int {
	// TODO Maybe the following can be simplified ¯\_(ツ)_/¯
	// Favor small values over larger ones by treating the sample space as a
	// squarish made up of two right triangles: a random point is sampled
	// from this, and the point's `y` coordinate will either be, the value
	// to return, or the exclusive maximum minus the value to return.
	//
	// Below is an example, a 5x6 squarish sample space, in which the exclusive
	// maximum is 5, with the return value displayed as a tile, and the value
	// for `y` displayed on the side:
	// ```
	// \00000 y=0
	// 4\1111 y=1
	// 33\222 y=2
	// 222\33 y=3
	// 1111\4 y=4
	// 00000\ y=5
	// ```
	val u = nextInt(until * (until + 1))
	val y = u / until
	return if (u % until >= y) y else until - y
}

fun Random.nextIntFavorSmallGreatly(until: Int): Int {
	return nextIntFavorSmall(nextIntFavorSmall(until) + 1)
}
