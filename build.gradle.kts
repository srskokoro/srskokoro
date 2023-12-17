import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.logic",
	"conventions.base",
	"conventions",
	"plugins",
).let { testableBuilds ->
	tasks {
		check { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
		register("test") { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
	}
}
