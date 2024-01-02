package build.root

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import build.plugins.test.buildProject
import build.plugins.test.io.TestTemp
import io.kotest.core.spec.style.FunSpec
import org.gradle.kotlin.dsl.*

class test__plugin : FunSpec({
	test("The 'root' plugin can only be applied to root projects") {
		val parent = buildProject(TestTemp.from(this, "0"))
		val child = buildProject(TestTemp.from(this, "0/0")) { withParent(parent) }

		assertAll {
			assertThat(runCatching { parent.apply<_plugin>() }).isSuccess()
			assertThat(runCatching { child.apply<_plugin>() }).isFailure()
		}
	}
})
