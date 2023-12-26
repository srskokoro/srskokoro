package build.plugins.test.io

import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object TestTemp {

	val base = File(System.getenv("TEST_TMPDIR")
		?: error("Environment variable `TEST_TMPDIR` not set up"))

	// --

	fun named(name: String) = File(base, name)

	fun of(testClass: Class<*>) = named(testClass.name)

	fun of(testClass: Class<*>, subPath: String) = named("${testClass.name}${File.separatorChar}$subPath")

	fun from(testInstance: Any) = of(testInstance::class.java)

	fun from(testInstance: Any, subPath: String) = of(testInstance::class.java, subPath)
}
