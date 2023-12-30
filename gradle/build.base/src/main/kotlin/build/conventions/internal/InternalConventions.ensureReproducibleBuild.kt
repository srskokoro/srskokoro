package build.conventions.internal

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.*

fun InternalConventions.ensureReproducibleBuild(project: Project): Unit = with(project) {
	tasks.withType<AbstractArchiveTask>().configureEach {
		// By default, don't include the version in the names of output archives.
		// - This clears the default behavior set by the `base` plugin.
		archiveVersion.convention(null as String?)

		// The following ensures that builds are reproducible.
		// - See, https://docs.gradle.org/8.2.1/userguide/working_with_files.html#sec:reproducible_archives
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
	}
}
