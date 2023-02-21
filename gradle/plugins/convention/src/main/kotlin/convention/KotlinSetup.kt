package convention

import convention.internal.KotlinTargetsConfigLoader
import convention.internal.util.*
import deps
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun Project.setUp(kotlin: KotlinProjectExtension): Unit = with(kotlin) {
	setUpProject(kotlin)
	jvmToolchain(deps.jvm.toolchainConfig)
}

private fun Project.setUpProject(kotlin: KotlinProjectExtension) {
	kotlinSourceSets = getSourceSets(kotlin)
}

internal fun Project.setUpTargetsViaConfig(kotlin: KotlinMultiplatformExtension) {
	val config = layout.projectDirectory.file("build.targets.txt")
	KotlinTargetsConfigLoader(providers, config).loadInto(kotlin)
}
