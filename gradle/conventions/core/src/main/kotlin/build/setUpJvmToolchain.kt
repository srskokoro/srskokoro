package build

import build.api.dsl.*
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun Project.setUpJvmToolchain(kotlin: KotlinProjectExtension) {
	val map = (deps ?: return).props.map
	kotlin.jvmToolchain { setUpFrom(map) }
}
