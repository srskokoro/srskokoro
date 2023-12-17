import build.api.dsl.*

plugins {
	id("build.root")
}

tasks.check { dependOnTaskFromIncludedBuildsOrFail() }
