package build.foundation

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

@InternalApi
object BuildFoundation {

	fun markOrFail(project: Project) {
		// NOTE: Extension named like this to discourage direct access.
		project.extensions.add<Any>("--BuildFoundation-mark--", BuildFoundation)
	}

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
}
