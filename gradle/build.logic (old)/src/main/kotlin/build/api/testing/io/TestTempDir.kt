package build.api.testing.io

import build.api.testing.TestSystemProps
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object TestTempDir {

	val base = File(System.getProperty(TestSystemProps.TMPDIR)
		?: error("System property \"${TestSystemProps.TMPDIR}\" not set up"))

	// --

	fun named(name: String) = File(base, name)

	fun of(testClass: Class<*>) = named(testClass.name)

	fun of(testClass: Class<*>, subPath: String) = named("${testClass.name}${File.separatorChar}$subPath")

	fun from(testInstance: Any) = of(testInstance::class.java)

	fun from(testInstance: Any, subPath: String) = of(testInstance::class.java, subPath)
}
