package build.dependencies

import build.api.support.io.DirectoryBuilder
import build.api.support.io.buildDir
import build.api.testing.gradleRunner
import build.api.testing.io.TestTempDir
import deps
import module
import org.gradle.api.Project
import plugin
import prop
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class test__plugin {

	@Test fun `A build with a chain of includes is working as expected`() {
		val mainProjectDir: File
		buildDir(TestTempDir.from(this)) {
			clean()
			setUpProject(
				"exportOnly",
				"""dependencySettings { exportOnly() }""",
				"""assertDepsNull()""",
			)
			setUpProject(
				"exportOnlyWithInclude",
				"""dependencySettings { exportOnly(); includeBuild("../exportOnly") }""",
				"""assertDepsNull()""",
			)
			setUpProject(
				"exportWithInclude",
				"""dependencySettings { export(); includeBuild("../exportOnlyWithInclude") }""",
				"""
				assertDepsSetup("exportOnly")
				assertDepsSetup("exportOnlyWithInclude")
				assertDepsSetup("exportWithInclude")
				""".trimIndent(),
			)
			setUpProject(
				"buildWithInclude",
				"""dependencySettings { includeBuild("../exportWithInclude") }""",
				"""
				assertDepsSetup("exportOnly")
				assertDepsSetup("exportOnlyWithInclude")
				assertDepsSetup("exportWithInclude")
				assertDepsSetup("buildWithInclude")
				""".trimIndent(),
			).run {
				mainProjectDir = file
			}
		}
		gradleRunner(mainProjectDir).build()
	}
}

/** @see setUpProject */
fun DependencySettings.setUpForTesting() {
	val name = settings.rootProject.name
	prop("$name.someProp", "$name.someValue")
	plugin("$name.somePlugin", "version-from-$name")
	module("$name:someModule", "version-from-$name")
}

/** @see setUpForTesting */
fun Project.assertDepsSetup(rootProjectName: String) {
	val d = deps ?: throw AssertionError("Expected `deps` to be set up")
	val v = d.versions

	@Suppress("UnnecessaryVariable")
	val name = rootProjectName
	assertEquals("$name.someValue", d.prop("$name.someProp"))
	assertEquals("version-from-$name", v.plugin("$name.somePlugin"))
	assertEquals("version-from-$name", v.module("$name:someModule"))
}

fun Project.assertDepsNull() {
	if (deps != null) throw AssertionError("Expected `deps` to be null")
}

private fun DirectoryBuilder.setUpProject(name: String, settingsConfig: String, rootProjectAction: String) = dir(name) {
	file("settings.gradle.kts").writeText(
		"""
		import build.dependencies.*
		plugins {
			id("build.dependencies")
		}
		dependencySettings {
			setUpForTesting()
		}
		""".trimIndent() + '\n' + settingsConfig + '\n' + """
		fun Project.setUpRootProjectForTesting() {
		""".trimIndent() + '\n' + rootProjectAction + '\n' + """
		}
		gradle.rootProject {
			extensions.add(typeOf(), "runTestSetupAction", Runnable { setUpRootProjectForTesting() })
		}
		""".trimIndent()
	)
	file("build.gradle.kts").writeText("runTestSetupAction.run()")
}
