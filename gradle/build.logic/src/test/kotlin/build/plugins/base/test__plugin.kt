package build.plugins.base

import build.api.testing.buildProject
import build.api.testing.io.TestTmpDir
import org.gradle.kotlin.dsl.*
import kotlin.test.Test
import kotlin.test.assertTrue

class test__plugin {

	@Test fun `The provided NOTE is still consistent with the expected plugins applied`() {
		val project = buildProject(TestTmpDir.from(this, "project"))
		project.apply<_plugin>()

		with(project.pluginManager) {
			assertTrue { hasPlugin("build.plugins.base") }
			assertTrue { hasPlugin("build.support.kt.jvm") }
			assertTrue { hasPlugin("build.support.kt.base") }
		}
	}
}