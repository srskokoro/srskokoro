package build.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals

class test_PluginId {

	@Test fun `of_unsafe(NAME) == of(NAME)`() {
		val id = "foobar"
		assertEquals(
			PluginId.of(id),
			PluginId.of_unsafe(id),
		)
	}

	@Test fun `of_unsafe(NS+ID) == of(NS+ID)`() {
		val id = "foo.bar"
		assertEquals(
			PluginId.of(id),
			PluginId.of_unsafe(id),
		)
	}
}
