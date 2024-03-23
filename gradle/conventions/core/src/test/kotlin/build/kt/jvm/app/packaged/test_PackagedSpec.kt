package build.kt.jvm.app.packaged

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.FreeSpec
import java.util.UUID

class test_PackagedSpec : FreeSpec({
	"UUID generation matches" {
		// UUID v5 generated from `NameSpace_URL + "app://<identifier>/"`
		// - Generated via, https://www.uuidtools.com/generate/v5
		val expect = UUID(0x0bdda1144b185e96, 0x8404ed0804ea6f37u.toLong())
		val actual = PackagedSpec_toPackageUuid(appNs = "com.example.foo.bar")
		assertThat(actual).isEqualTo(expect)
	}
})
