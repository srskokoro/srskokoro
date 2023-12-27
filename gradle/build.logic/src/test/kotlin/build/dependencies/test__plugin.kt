package build.dependencies

import build.plugins.test.gradleRunner
import build.plugins.test.io.TestTemp
import build.support.io.DirectoryBuilder
import build.support.io.buildDir
import deps
import module
import org.gradle.api.Project
import plugin
import prop
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class test__plugin {

	@Test fun `A build with a chain of includes is working as expected`() {
		val mainProjectDir: File
		buildDir(TestTemp.from(this, "0")) {
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

	@Test fun `Export is preserved as expected`() {
		val projectDir = buildDir(TestTemp.from(this, "1")).run {
			setUpProject("project", "dependencySettings { exportOnly() }")
				.file
		}

		// Ensure that the settings file's last modification time is less than
		// the current time.
		Thread.sleep(1)

		val runner = gradleRunner(projectDir)
		assertContains(
			runner.build().output,
			"Generated new dependency settings export: "
		)
		assertContains(
			runner.build().output,
			"Preserved likely up-to-date dependency settings export: "
		)
	}

	@Test fun `Should fail on empty include`() {
		buildDir(TestTemp.from(this, "2")).run {
			dir("empty")
			setUpProject("project", """dependencySettings { includeBuild("../empty") }""")
				.file
		}.let {
			gradleRunner(it).buildAndFail()
		}
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

private fun DirectoryBuilder.setUpProject(name: String, settingsConfig: String, rootProjectAction: String = "") = dir(name) {
	file("settings.gradle.kts").writeText(
		"""
		import build.dependencies.*
		plugins {
			id("build.dependencies")
		}
		dependencySettings {
			setUpForTesting()
		}
		""".trimIndent() + '\n' + settingsConfig + if (rootProjectAction.isEmpty()) "" else '\n' + """
		fun Project.setUpRootProjectForTesting() {
		""".trimIndent() + '\n' + rootProjectAction + '\n' + """
		}
		gradle.rootProject {
			extensions.add(typeOf(), "runTestSetupAction", Runnable { setUpRootProjectForTesting() })
		}
		""".trimIndent()
	)
	if (rootProjectAction.isNotEmpty())
		file("build.gradle.kts").writeText("runTestSetupAction.run()")
}
