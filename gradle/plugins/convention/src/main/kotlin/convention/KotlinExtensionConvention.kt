package convention

import deps
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun KotlinProjectExtension.setUpConvention() {
	jvmToolchain(deps.jvm.toolchainConfig)
}
