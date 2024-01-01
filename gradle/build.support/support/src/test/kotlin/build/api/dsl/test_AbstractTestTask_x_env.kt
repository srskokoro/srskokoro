package build.api.dsl

import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AbstractTestTask_x_env {

	@Test fun `Extension name used is consistent`() {
		@OptIn(InternalConventionsApi::class)
		assertEquals(env__extension, InternalConventions.env__extension)
	}
}
