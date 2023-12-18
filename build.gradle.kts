import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.logic",
	"conventions.base",
	"plugins",
).let { testableBuilds ->
	tasks {
		check { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
		maybeRegisterTestTask { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }

		clean { dependOnTaskFromIncludedBuildsOrFail(testableBuilds) }
	}
}
