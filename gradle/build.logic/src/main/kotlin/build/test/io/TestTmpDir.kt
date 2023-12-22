package build.test.io

import org.gradle.testfixtures.ProjectBuilder
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object TestTmpDir {

	internal const val TEST_IO_TMPDIR_SYS_PROP = "build.test.io.tmpdir"

	val base = File(System.getProperty(TEST_IO_TMPDIR_SYS_PROP)
		?: error("System property \"$TEST_IO_TMPDIR_SYS_PROP\" not set up"))

	// --

	fun named(name: String) = File(base, name).apply { mkdirs() }

	fun of(testClass: Class<*>) = named(testClass.name)

	fun of(testClass: Class<*>, subPath: String) = named("${testClass.name}${File.separatorChar}$subPath")

	fun from(testInstance: Any) = of(testInstance::class.java)

	fun from(testInstance: Any, subPath: String) = of(testInstance::class.java, subPath)

	// --

	private object _gradleUserHome {
		val value = named(".gradleUserHome")
	}

	val gradleUserHome: File get() = _gradleUserHome.value

	fun projectBuilder() = ProjectBuilder.builder().withGradleUserHomeDir(gradleUserHome)

	fun projectBuilder(projectDir: File?) = projectBuilder().withProjectDir(projectDir)

	inline fun buildProject(configure: ProjectBuilder.() -> Unit = {}) =
		projectBuilder().apply(configure).build()

	inline fun buildProject(projectDir: File?, configure: ProjectBuilder.() -> Unit = {}) =
		projectBuilder(projectDir).apply(configure).build()
}
