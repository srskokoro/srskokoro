package build.plugins.test

import kotlin.test.Test
import kotlin.test.assertNotNull

class test_gradleRunner {

	@Test fun `Environment variable for plugin classpath is set up automatically`() {
		assertNotNull(System.getenv(gradleRunner_PLUGIN_CLASSPATH_ENV_VAR))
	}
}
