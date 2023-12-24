/*
import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.logic",
	"support",
	"plugins",
).let { testableBuilds ->
	tasks {
		check { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
		registerTestTask { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }

		clean { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
	}
}
*/
