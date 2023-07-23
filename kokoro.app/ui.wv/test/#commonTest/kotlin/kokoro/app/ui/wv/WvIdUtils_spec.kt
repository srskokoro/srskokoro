package kokoro.app.ui.wv

import assert
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.string.shouldHaveMaxLength
import io.kotest.matchers.string.shouldHaveMinLength
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.ArbitraryBuilder
import io.kotest.property.arbitrary.IntShrinker
import io.kotest.property.arbitrary.numbers.IntClassifier
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.ints
import io.kotest.property.forAll

class WvIdUtils_spec : FunSpec({
	test("The encoding map is exactly $wvIdEncodeMap_size in size") {
		wvIdEncodeMap.size.shouldBeExactly(wvIdEncodeMap_size)
	}
	test("All characters in the encoding map are ASCII characters") {
		checkAll(Exhaustive.ints(0 until wvIdEncodeMap_size)) { i ->
			wvIdEncodeMap[i].code.shouldBeLessThan(ASCII_TABLE_SIZE)
		}
	}
	test("The first characters of the encoding map, up to $MAX_SLOT inclusive, are valid starting characters in a dot notation property access in JS") {
		forAll(Exhaustive.ints(0..MAX_SLOT)) { i ->
			val c = wvIdEncodeMap[i]

			// Quote:
			// > An identifier must start with `$`, `_`, or any character in the
			// > Unicode categories “Uppercase letter (Lu)”, “Lowercase letter
			// > (Ll)”, “Titlecase letter (Lt)”, “Modifier letter (Lm)”, “Other
			// > letter (Lo)”, or “Letter number (Nl)”.
			// -- from, https://stackoverflow.com/a/9337047
			//
			// NOTE: Although JS allows `$` and `_`, we're reserving them for
			// special use and should not be allowed here.
			c.isLetter()
		}
	}
	test("Output of `appendWvElemId` is as expected") {
		// Use a custom `Arb<Int>` that favors values close to zero
		val intRange = Int.MIN_VALUE..Int.MAX_VALUE
		val intArb = ArbitraryBuilder.create { rs ->
			val rn = rs.random
			val mask = -1 ushr rn.nextInt(Int.SIZE_BITS)
			rn.nextInt() and mask
		}.withEdgecases(listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE))
			.withShrinker(IntShrinker(intRange))
			.withClassifier(IntClassifier(intRange))
			.build()

		checkAll(intArb) { elemId ->
			val elemIdStr = buildString {
				appendWvElemId(this, elemId)
			}
			assertSoftly {
				elemIdStr
					.shouldHaveMinLength(2)
					.shouldHaveMaxLength(6)
			}
			parseWvElemId(elemIdStr)
				.shouldBeExactly(elemId)
		}
	}
})

private const val wvIdEncodeMap_size = 64

private const val ASCII_TABLE_SIZE = 128

@Suppress("DEPRECATION")
private val wvIdEncodeMap = `-TestAccess-wvIdEncodeMap`()

private val wvIdDecodeMap = ByteArray(ASCII_TABLE_SIZE) { -1 }.also { dcm ->
	for ((i, c) in wvIdEncodeMap.withIndex()) {
		val cc = c.code
		if (cc < ASCII_TABLE_SIZE) {
			dcm[cc] = i.toByte()
		}
	}
}

private fun parseWvElemId(src: String): Int {
	val n = src.length
	assert { n in 2..6 }

	val dcm = wvIdDecodeMap
	var i = 0

	var res = src[i++].code.let { cc ->
		assert { cc <= 127 }
		val r = dcm[cc and 127].toInt()
		assert { r <= 31 }
		r and 31
	}

	var sh = -1
	do {
		val cc = src[i++].code
		assert { cc <= 127 }
		val r = dcm[cc and 127].toInt()
		sh += 6
		res = res or (r shl sh)
	} while (i < n)

	return res
}
