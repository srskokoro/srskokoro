package build

import build.api.dsl.*
import deps
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun Project.setUpJvmToolchain(kotlin: KotlinProjectExtension) {
	kotlin.jvmToolchain(jvmToolchainSetupFrom((deps ?: return).props.map))
}
