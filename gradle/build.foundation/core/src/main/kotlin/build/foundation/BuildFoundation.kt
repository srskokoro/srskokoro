package build.foundation

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.kotlin.dsl.*

@InternalApi
object BuildFoundation {

	// Named like this to discourage direct access
	private const val mark__extension = "--BuildFoundation-mark--"

	fun markOrFail(project: Project) {
		project.extensions.add<Any>(mark__extension, BuildFoundation)
	}

	fun isMarked(project: Project): Boolean =
		project.extensions.findByName(mark__extension) != null

	// --

	/**
	 * An environment variable specifying a path to a custom temporary directory
	 * that acts as a sandbox for the test task to play in without fear, for
	 * when doing tests with the filesystem.
	 */
	// NOTE: A utility somewhere else in our build uses this and expects it to
	// be set up automatically.
	const val TEST_TMPDIR = "TEST_TMPDIR"

	// Used as extension name. It's named like this to discourage direct access.
	const val env__extension = "--_env_--"

	// --

	@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
	inline fun ObjectConfigurationAction.kotlin(module: String) =
		"org.jetbrains.kotlin.$module"

	fun DependencyHandler.compileOnlyTestImpl(dependencyNotation: Any) {
		add("compileOnly", dependencyNotation)
		add("testImplementation", dependencyNotation)
	}

	// --

	/**
	 * The following are a bunch of constants whose usage we would like to keep
	 * track of via the IDE's refactoring mechanism.
	 */
	object MPP {
		const val jvmish = "jvmish"
		const val unix = "unix"
		const val desktop = "desktop"
		const val mobile = "mobile"
	}
}
