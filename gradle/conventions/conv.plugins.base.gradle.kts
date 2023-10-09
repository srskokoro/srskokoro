plugins {
	base
}

tasks.withType<AbstractArchiveTask>().configureEach {
	// The following ensures that builds are reproducible.
	// - See, https://docs.gradle.org/8.2.1/userguide/working_with_files.html#sec:reproducible_archives
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}
