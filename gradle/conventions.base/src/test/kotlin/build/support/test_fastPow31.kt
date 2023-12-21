package build.support

import kotlin.test.Test
import kotlin.test.assertContentEquals

class test_fastPow31 {

	companion object {
		@Suppress("INVISIBLE_MEMBER")
		private val TEST_RANGE = 0 until fastPow31Table_n * 2
	}

	private fun naivePow31Calc(n: Int): Int {
		var x = 1
		for (i in 0 until n) x *= 31
		return x
	}

	@Suppress("INVISIBLE_MEMBER")
	@Test fun `fastPow31(N) == naivePow31Calc(N)`() {
		assertContentEquals(
			TEST_RANGE.map { naivePow31Calc(it) },
			TEST_RANGE.map { fastPow31(it) },
		)
	}

	@Suppress("INVISIBLE_MEMBER")
	@Test fun `fastPow31Calc(N) == fastPow31(N)`() {
		assertContentEquals(
			TEST_RANGE.map { fastPow31(it) },
			TEST_RANGE.map { fastPow31Calc(it) },
		)
	}
}
