plugins {
	id("build.root")
}

tasks.check { dependOnTaskFromIncludedBuildsOrFail() }
