package build.support

import kotlin.test.Test
import kotlin.test.assertContentEquals

class `test fastPow31` {
	companion object {
		private val TEST_RANGE = 0 until 256
	}

	private fun naivePow31Calc(n: Int): Int {
		var x = 1
		for (i in 0 until n) x *= 31
		return x
	}

	@Test fun `fastPow31Calc(N) == naivePow31Calc(N)`() {
		assertContentEquals(
			TEST_RANGE.map { naivePow31Calc(it) },
			TEST_RANGE.map { fastPow31Calc(it) },
		)
	}

	@Test fun `fastPow31(N) == fastPow31Calc(N)`() {
		assertContentEquals(
			TEST_RANGE.map { fastPow31Calc(it) },
			TEST_RANGE.map { fastPow31(it) },
		)
	}
}
