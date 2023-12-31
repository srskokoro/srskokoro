package build.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals

class test_ModuleId {

	@Test fun `of_unsafe(ID) == of(ID)`() {
		val id = "foo:bar"
		assertEquals(
			ModuleId.of(id),
			ModuleId.of_unsafe(id),
		)
	}

	@Test fun `of_unsafe(GROUP, NAME) == of(GROUP, NAME)`() {
		val group = "foo"
		val name = "bar"
		assertEquals(
			ModuleId.of(group, name),
			ModuleId.of_unsafe(group, name),
		)
	}

	@Test fun `of(ID) == of(GROUP, NAME)`() {
		val group = "foo"
		val name = "bar"
		val id = "foo:bar"

		val viaId = ModuleId.of(id)
		val viaGroupName = ModuleId.of(group, name)

		assertEquals(viaGroupName, viaId)
		assertEquals(viaGroupName.hashCode(), viaId.hashCode())
		assertEquals(viaGroupName.toString(), viaId.toString())

		assertEquals(viaGroupName.group, viaId.group)
		assertEquals(viaGroupName.name, viaId.name)
	}
}
