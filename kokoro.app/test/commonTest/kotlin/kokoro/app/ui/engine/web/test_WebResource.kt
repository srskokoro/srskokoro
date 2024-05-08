package kokoro.app.ui.engine.web

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isNotSameInstanceAs
import io.kotest.core.spec.style.FreeSpec

class test_WebResource : FreeSpec({
	"Must not be the same instances" {
		assertAll {
			assertThat(WebResource.EMPTY)
				.isNotSameInstanceAs(WebResource())
			assertThat(WebResource())
				.isNotSameInstanceAs(WebResource())
		}
	}
})
