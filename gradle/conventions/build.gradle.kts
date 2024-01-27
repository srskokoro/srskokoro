import build.api.dsl.*
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
	`kotlin-dsl-base` apply false
	id("build.conventions.root")
	id("build.conventions.api")
}

gradle.includedBuilds(
	"core",
	"build.foundation",
).let { builds ->
	tasks {
		check { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
		clean { dependOnSameTaskFromIncludedBuildsOrFail(builds) }
	}
}

allprojects(fun Project.() {
	val deps = deps ?: return
	val jvmToolchainSetup = Action<JavaToolchainSpec> {
		setUpFrom(deps.props.map)
		restrictVersionForBuildInclusive()
	}
	prioritizedAfterEvaluate(fun Project.() {
		@OptIn(InternalApi::class)
		if (!BuildFoundation.isMarked(this)) return
		(extensions.findByName("kotlin") as KotlinProjectExtension?)
			?.jvmToolchain(jvmToolchainSetup)
	})
})
