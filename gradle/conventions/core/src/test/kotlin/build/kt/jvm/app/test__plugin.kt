package build.kt.jvm.app

import assertk.assertThat
import assertk.assertions.isNotNull
import build.api.dsl.accessors.application
import build.api.dsl.accessors.jvmArgs
import build.plugins.test.buildProject
import build.plugins.test.io.TestTemp
import build.test.hasExactly
import io.kotest.core.spec.style.FreeSpec
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.kotlin.dsl.*

class test__plugin : FreeSpec({
	"The JVM args are as expected." {
		buildProject(TestTemp()).run {
			apply(fun(x) { x.plugin<_plugin>() })

			val expectedJvmArgs = arrayOf("-Dfoo=bar")
				.asList().also { application.jvmArgs.addAll(it) }
				.let { ArrayList(it) }.apply { add("-ea") }

			(this as ProjectInternal).evaluate()
			val tasks = tasks

			tasks.getByName<JavaExec>("run").run {
				assertThat(jvmArgs).isNotNull().hasExactly(expectedJvmArgs)
			}
			tasks.getByName<JavaExec>("runShadow").run {
				assertThat(jvmArgs).isNotNull().hasExactly(expectedJvmArgs)
			}

			tasks.getByName<CreateStartScripts>("startScripts").run {
				assertThat(defaultJvmOpts).isNotNull().hasExactly(expectedJvmArgs)
			}
			tasks.getByName<CreateStartScripts>("startShadowScripts").run {
				assertThat(defaultJvmOpts).isNotNull().hasExactly(expectedJvmArgs)
			}
		}
	}
})
