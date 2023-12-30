package build.plugins.test.io

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class test_TestTemp {

	@Test fun `Environment variable holding the path for temporaries is set up automatically`() {
		val path: String? = System.getenv(TEST_TMPDIR)
		assertNotNull(path)
		assertEquals(TestTemp.base, File(path))
	}
}
