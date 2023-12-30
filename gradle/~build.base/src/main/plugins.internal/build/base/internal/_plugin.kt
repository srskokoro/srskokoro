package build.base.internal

import build.api.ProjectPlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	tasks.withType<AbstractArchiveTask>().configureEach {
		// By default, don't include the version in the names of output archives.
		// - This clears the default behavior set by the `base` plugin.
		archiveVersion.convention(null as String?)

		// The following ensures that builds are reproducible.
		// - See, https://docs.gradle.org/8.2.1/userguide/working_with_files.html#sec:reproducible_archives
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
	}
})
