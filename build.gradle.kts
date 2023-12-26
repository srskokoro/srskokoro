import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.base",
	"build.logic",
).let { testableBuilds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(testableBuilds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(testableBuilds) }
	}
}
