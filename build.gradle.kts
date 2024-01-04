import build.api.dsl.*

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"build.foundation",
	"conventions",
	"multipurpose",
).let { builds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
	}
}
