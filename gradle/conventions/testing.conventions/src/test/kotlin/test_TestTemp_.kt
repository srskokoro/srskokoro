package build.plugins.test.io

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.FreeSpec
import java.io.File

class test_TestTemp_ : FreeSpec({
	"Directory path is as expected" {
		val expected = File(TestTemp.base, test_TestTemp_::class.java.name)
		assertThat(TestTemp()).isEqualTo(expected)
		assertThat(TestTemp("0")).isEqualTo(File(expected, "0"))
	}
})
