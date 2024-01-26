import build.api.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
	id("build.root")
}

gradle.includedBuilds(
	"conventions",
	"hoisted",
	"inclusives",
	"plugins",
).let { builds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
		builds.filter { File(it.projectDir, "#kotlin-js-store").exists() }.let(fun(builds) {
			withType<KotlinNpmInstallTask>().configureEach {
				dependOnSameTaskFromIncludedBuildsOrFail(builds)
			}
		})
	}
}
