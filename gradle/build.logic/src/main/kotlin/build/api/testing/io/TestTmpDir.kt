package build.api.testing.io

import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object TestTmpDir {

	internal const val TEST_TMPDIR_SYS_PROP = "build.api.testing.io.tmpdir"

	val base = File(System.getProperty(TEST_TMPDIR_SYS_PROP)
		?: error("System property \"$TEST_TMPDIR_SYS_PROP\" not set up"))

	// --

	fun named(name: String) = File(base, name).apply { mkdirs() }

	fun of(testClass: Class<*>) = named(testClass.name)

	fun of(testClass: Class<*>, subPath: String) = named("${testClass.name}${File.separatorChar}$subPath")

	fun from(testInstance: Any) = of(testInstance::class.java)

	fun from(testInstance: Any, subPath: String) = of(testInstance::class.java, subPath)
}
