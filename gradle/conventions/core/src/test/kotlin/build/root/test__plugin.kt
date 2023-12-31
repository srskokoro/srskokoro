package build.root

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import build.plugins.test.buildProject
import build.plugins.test.io.TestTemp
import org.gradle.kotlin.dsl.*
import kotlin.test.Test

class test__plugin {

	@Test fun `The 'root' plugin can only be applied to root projects`() {
		val parent = buildProject(TestTemp.from(this, "0"))
		val child = buildProject(TestTemp.from(this, "0/0")) { withParent(parent) }

		assertAll {
			assertThat(runCatching { parent.apply<_plugin>() }).isSuccess()
			assertThat(runCatching { child.apply<_plugin>() }).isFailure()
		}
	}
}
