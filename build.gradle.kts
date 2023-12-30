/*
import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.logic",
	"conventions",
	"plugins",
	"support",
).let { testableBuilds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(testableBuilds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(testableBuilds) }
	}
}
*/
