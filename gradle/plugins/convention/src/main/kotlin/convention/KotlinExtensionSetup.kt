package convention

import deps
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun setUp(extension: KotlinProjectExtension): Unit = with(extension) {
	jvmToolchain(deps.jvm.toolchainConfig)
}
