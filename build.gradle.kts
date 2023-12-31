import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.support",
	"conventions",
).let { builds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
	}
}
