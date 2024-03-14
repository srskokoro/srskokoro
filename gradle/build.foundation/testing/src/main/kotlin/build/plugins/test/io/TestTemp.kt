package build.plugins.test.io

import java.io.File
import kotlin.reflect.KClass

object TestTemp {
	/**
	 * An environment variable specifying a path to a custom temporary directory
	 * that acts as a sandbox for the test task to play in without fear, for
	 * when doing tests with the filesystem.
	 */
	internal const val TEST_TMPDIR = "TEST_TMPDIR"

	val base = File(System.getenv(TEST_TMPDIR)
		?: error("Environment variable `$TEST_TMPDIR` not set up"))

	// --

	fun named(name: String) = File(base, name)

	fun of(testClass: Class<*>) = named(testClass.name)

	fun of(testClass: Class<*>, subPath: String) = named("${testClass.name}${File.separatorChar}$subPath")

	@Suppress("NOTHING_TO_INLINE")
	inline fun of(testClass: KClass<*>) = of(testClass.java)

	@Suppress("NOTHING_TO_INLINE")
	inline fun of(testClass: KClass<*>, subPath: String) = of(testClass.java, subPath)

	fun from(testInstance: Any) = of(testInstance::class.java)

	fun from(testInstance: Any, subPath: String) = of(testInstance::class.java, subPath)
}
