import build.api.dsl.*

plugins {
	`kotlin-dsl-base` apply false
	id("build.conventions.root")
	id("build.conventions.api")
}

gradle.includedBuilds(
	"build.base",
	"build.support",
).let { builds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
	}
}
