package build.plugins.test.io

import build.foundation.BuildFoundation
import build.foundation.InternalApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class test_TestTemp {

	@Test fun `Environment variable name used is consistent`() {
		@OptIn(InternalApi::class)
		assertEquals(TestTemp.TEST_TMPDIR, BuildFoundation.TEST_TMPDIR)
	}

	@Test fun `Environment variable holding the path for temporaries is set up automatically`() {
		val path: String? = System.getenv(TestTemp.TEST_TMPDIR)
		assertNotNull(path)
		assertEquals(TestTemp.base, File(path))
	}
}
