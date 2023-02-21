package convention

import convention.internal.KotlinTargetsConfigLoader
import deps
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun setUp(extension: KotlinProjectExtension): Unit = with(extension) {
	jvmToolchain(deps.jvm.toolchainConfig)
}

internal fun Project.setUpTargetsViaConfig(kotlin: KotlinMultiplatformExtension) {
	val config = layout.projectDirectory.file("build.targets.txt")
	KotlinTargetsConfigLoader(providers, config).loadInto(kotlin)
}
