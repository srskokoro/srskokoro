package build.api.dsl

import build.foundation.BuildFoundation
import build.foundation.InternalApi
import kotlin.test.Test
import kotlin.test.assertEquals

class test_AbstractTestTask_x_env {

	@Test fun `Extension name used is consistent`() {
		@OptIn(InternalApi::class)
		assertEquals(env__extension, BuildFoundation.env__extension)
	}
}
