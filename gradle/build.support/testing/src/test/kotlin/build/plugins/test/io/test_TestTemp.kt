package build.plugins.test.io

import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class test_TestTemp {

	@Test fun `Environment variable name used is consistent`() {
		@OptIn(InternalConventionsApi::class)
		assertEquals(TestTemp.TEST_TMPDIR, InternalConventions.TEST_TMPDIR)
	}

	@Test fun `Environment variable holding the path for temporaries is set up automatically`() {
		val path: String? = System.getenv(TestTemp.TEST_TMPDIR)
		assertNotNull(path)
		assertEquals(TestTemp.base, File(path))
	}
}
