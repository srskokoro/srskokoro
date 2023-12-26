package build.plugins.test

import kotlin.test.Test
import kotlin.test.assertNotNull

class test_gradleRunner {

	@Test fun `System property for classpath is set up`() {
		assertNotNull(System.getProperty(gradleRunner_CLASSPATH_SYS_PROP))
	}
}
