package convention

import deps
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun KotlinProjectExtension.configureConvention() {
	jvmToolchain(deps.jvm.toolchainConfig)
}
