plugins {
	base
}

tasks.withType<AbstractArchiveTask>().configureEach {
	// By default, don't include the version in the names of output archives.
	// - This clears the default behavior set by the `base` plugin.
	archiveVersion.convention(null as String?)
}

configurations.all {
	if (isCanBeResolved) {
		// Fail on transitive upgrade/downgrade of direct dependency versions
		failOnDirectDependencyVersionGotcha(gradle)
	}
}
