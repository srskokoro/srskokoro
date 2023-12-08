package build.support

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class `test hashCodeWith` {

	@Test fun `X hashCodeWith(X)`() = assertAll {
		val hashOfFooBar = "FooBar".hashCode()

		assertThat("Foo".hashCodeWith("Bar")).isEqualTo(hashOfFooBar)
		assertThat("Foo".hashCode().hashCodeWith("Bar")).isEqualTo(hashOfFooBar)

		assertThat("FooBa".hashCodeWith('r')).isEqualTo(hashOfFooBar)
		assertThat("FooBa".hashCode().hashCodeWith('r')).isEqualTo(hashOfFooBar)

		assertThat('F'.hashCodeWith("ooBar")).isEqualTo(hashOfFooBar)
		assertThat('F'.hashCode().hashCodeWith("ooBar")).isEqualTo(hashOfFooBar)

		assertThat('A'.hashCodeWith('B')).isEqualTo("AB".hashCode())
	}
}
