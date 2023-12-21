package build.api.support

import kotlin.test.Test
import kotlin.test.assertEquals

class test_hashCodeWith {

	companion object {
		private const val X = "Foo"
		private const val Y = "Bar"
		private const val XY = "$X$Y"
		private val XY_hash = XY.hashCode()
	}

	// --

	@Test fun `X hashCodeWith(Y) == XY hashCode()`() =
		assertEquals(XY_hash, X.hashCodeWith(Y))

	@Test fun `X hashCode() hashCodeWith(Y) == XY hashCode()`() =
		assertEquals(XY_hash, X.hashCode().hashCodeWith(Y))

	// --

	@Test fun `XY dropLast(1) hashCodeWith(XY last()) == XY hashCode()`() =
		assertEquals(XY_hash, XY.dropLast(1).hashCodeWith(XY.last()))

	@Test fun `XY dropLast(1) hashCode() hashCodeWith(XY last()) == XY hashCode()`() =
		assertEquals(XY_hash, XY.dropLast(1).hashCode().hashCodeWith(XY.last()))

	// --

	@Test fun `XY first() hashCodeWith(XY drop(1)) == XY hashCode()`() =
		assertEquals(XY_hash, XY.first().hashCodeWith(XY.drop(1)))

	@Test fun `XY first() hashCode() hashCodeWith(XY drop(1)) == XY hashCode()`() =
		assertEquals(XY_hash, XY.first().hashCode().hashCodeWith(XY.drop(1)))

	// --

	@Test fun `'A' hashCodeWith('B') == 'AB' hashCode()`() =
		assertEquals("AB".hashCode(), 'A'.hashCodeWith('B'))
}
