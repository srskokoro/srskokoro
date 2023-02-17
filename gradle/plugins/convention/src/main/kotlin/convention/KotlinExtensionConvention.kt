package convention

import deps
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun KotlinProjectExtension.setUpConvention() {
	jvmToolchain(deps.jvm.toolchainConfig)
}
