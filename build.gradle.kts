import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"conventions",
	"inclusives",
	"plugins",
).let { builds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
	}
}
