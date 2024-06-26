import build.api.dsl.*
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

plugins {
	`kotlin-dsl-base` apply false

	id("build.conventions.root")
	id("build.conventions.api")

	id("io.kotest.multiplatform") apply false
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
		(extensions.findByName("kotlin") as KotlinTopLevelExtension?)
			?.jvmToolchain(jvmToolchainSetup)
	})
})
