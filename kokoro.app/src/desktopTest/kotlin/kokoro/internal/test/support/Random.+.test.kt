package kokoro.internal.test.support

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.checkAll
import io.kotest.property.forAll

class `Random ~ (Test)` : FunSpec({
	test("`nextIntFavorSmall(u)` returns `0` until `u`") {
		forAll(arbitrary { rs ->
			rs.random.nextIntFavorSmall(rs.random.nextInt(1, 11))
		}) { it in 0 until 10 }
	}
	test("`nextString()` respects the specified length") {
		checkAll(arbitrary { rs ->
			val expectedLength = rs.random.nextInt(0, 11)
			rs.random.nextString(expectedLength) to expectedLength
		}) { (str, length) ->
			str.length.shouldBeExactly(length)
		}
	}
})
