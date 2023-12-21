import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.logic",
	"plugins",
).let { testableBuilds ->
	tasks {
		check { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
		registerTestTask { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }

		clean { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
	}
}
