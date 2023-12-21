package build.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals

class test_ModuleId {

	@Suppress("INVISIBLE_MEMBER")
	@Test fun `of_unsafe(ID) == of(ID)`() {
		val id = "foo:bar"
		assertEquals(
			ModuleId.of(id),
			ModuleId.of_unsafe(id),
		)
	}

	@Suppress("INVISIBLE_MEMBER")
	@Test fun `of_unsafe(GROUP, NAME) == of(GROUP, NAME)`() {
		val group = "foo"
		val name = "bar"
		assertEquals(
			ModuleId.of(group, name),
			ModuleId.of_unsafe(group, name),
		)
	}
}
